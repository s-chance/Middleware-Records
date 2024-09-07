package org.entropy.restclient.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.entropy.restclient.dto.RequestParams;
import org.entropy.restclient.mapper.HotelMapper;
import org.entropy.restclient.pojo.Hotel;
import org.entropy.restclient.pojo.HotelDoc;
import org.entropy.restclient.vo.PageResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> {
    private final RestHighLevelClient client;

    public HotelService(RestHighLevelClient client) {
        this.client = client;
    }

    public PageResult search(RequestParams params) {
        SearchRequest request = new SearchRequest("hotel");
        // 多条件搜索
        BoolQueryBuilder boolQuery = getBoolQueryBuilder(params);
        request.source().query(boolQuery);

        // 距离排序
        String location = params.getLocation();
        if (location != null && !location.isEmpty()) {
            request.source()
                    .sort(SortBuilders
                            .geoDistanceSort("location", new GeoPoint(location))
                            .order(SortOrder.ASC)
                            .unit(DistanceUnit.KILOMETERS));
        }

        int page = params.getPage();
        int size = params.getSize();
        request.source()
                .from((page - 1) * size)
                .size(size);

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD", true),
                                ScoreFunctionBuilders.weightFactorFunction(5)
                        )
                }
        );
        request.source().query(functionScoreQueryBuilder);

        SearchResponse search = null;
        try {
            search = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return handleResponse(search);
    }

    private static BoolQueryBuilder getBoolQueryBuilder(RequestParams params) {
        // boolean query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String key = params.getKey();
        if (key == null || key.isEmpty()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("all", key));
        }
        // city精确匹配
        if (params.getCity() != null && !params.getCity().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("city", params.getCity()));
        }
        // brand精确匹配
        if (params.getBrand() != null && !params.getBrand().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand", params.getBrand()));
        }
        // starName精确匹配
        if (params.getStarName() != null && !params.getStarName().isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("starName", params.getStarName()));
        }
        // price范围过滤
        if (params.getMinPrice() != null && params.getMaxPrice() != null) {
            boolQuery.filter(QueryBuilders
                    .rangeQuery("price")
                    .gte(params.getMinPrice())
                    .lte(params.getMaxPrice()));
        }
        return boolQuery;
    }

    private static PageResult handleResponse(SearchResponse search) {
        SearchHits searchHits = search.getHits();
        long total = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();

            // 获取source
            ObjectMapper mapper = new ObjectMapper();
            HotelDoc hotelDoc = null;
            try {
                hotelDoc = mapper.readValue(json, HotelDoc.class);
                // 获取距离排序值
                Object[] sortValues = hit.getSortValues();
                if (sortValues.length > 0) {
                    Object sortValue = sortValues[0];
                    hotelDoc.setDistance(sortValue);
                }
                // 获取算分
                float score = hit.getScore();
                hotelDoc.set_score(score);
                hotels.add(hotelDoc);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return new PageResult(total, hotels);
    }

    public Map<String, List<String>> filters(RequestParams params) {
        SearchRequest request = new SearchRequest("hotel");
        // 多条件搜索
        BoolQueryBuilder boolQuery = getBoolQueryBuilder(params);
        request.source().query(boolQuery);

        // 聚合
        buildAggregation(request);
        SearchResponse search = null;
        try {
            search = client.search(request, RequestOptions.DEFAULT);
            Map<String, List<String>> result = new HashMap<>();
            Aggregations aggregations = search.getAggregations();
            List<String> brandList = getAggByName(aggregations, "brandAgg");
            List<String> cityList = getAggByName(aggregations, "cityAgg");
            List<String> starList = getAggByName(aggregations, "starAgg");
            result.put("品牌", brandList);
            result.put("城市", cityList);
            result.put("星级", starList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getAggByName(Aggregations aggregations, String aggName) {
        Terms brandAgg = aggregations.get(aggName);
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            String key = bucket.getKeyAsString();
            brandList.add(key);
        }
        return brandList;
    }

    private static void buildAggregation(SearchRequest request) {
        request.source().size(0);
        request.source()
                .aggregation(AggregationBuilders
                        .terms("brandAgg")
                        .field("brand")
                        .size(10));
        request.source()
                .aggregation(AggregationBuilders
                        .terms("cityAgg")
                        .field("brand")
                        .size(10));
        request.source()
                .aggregation(AggregationBuilders
                        .terms("starAgg")
                        .field("starName")
                        .size(10));
    }
}