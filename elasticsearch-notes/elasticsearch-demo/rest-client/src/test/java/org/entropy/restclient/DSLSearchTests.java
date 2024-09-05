package org.entropy.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.entropy.restclient.pojo.Hotel;
import org.entropy.restclient.pojo.HotelDoc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

public class DSLSearchTests {

    private RestHighLevelClient client;

    @Test
    void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .query(QueryBuilders.matchAllQuery());
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    void testMatch() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .query(QueryBuilders.matchQuery("all", "如家"));
        request.source()
                .query(QueryBuilders.termQuery("city", "上海"));
        request.source()
                .query(QueryBuilders.rangeQuery("price").gte(100).lte(150));
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    void testBool() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        // 创建布尔查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 设置must条件
        boolQuery.must(QueryBuilders.termQuery("city", "上海"));
        // 设置filter条件
        boolQuery.filter(QueryBuilders.rangeQuery("price").lte(250));
        request.source().query(boolQuery);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    void testPageAndSort() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        int page = 2, size = 5;
        request.source()
                .query(QueryBuilders.matchAllQuery())
                .from((page - 1) * size)
                .size(size)
                .sort("price", SortOrder.ASC);
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    @Test
    void testHighlight() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .query(QueryBuilders.matchQuery("all", "如家"))
                .highlighter(new HighlightBuilder()
                        .field("name")
                        .requireFieldMatch(false));
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        handleResponse(search);
    }

    private static void handleResponse(SearchResponse search) throws JsonProcessingException {
        SearchHits searchHits = search.getHits();
        long total = searchHits.getTotalHits().value;
        System.out.println(total);
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();

            // 获取source
            ObjectMapper mapper = new ObjectMapper();
            HotelDoc hotelDoc = mapper.readValue(json, HotelDoc.class);
            // 处理高亮
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                // 获取高亮的字段
                HighlightField highlightField = highlightFields.get("name");
                if (highlightField != null) {
                    // 使用高亮字段中的值替换source中对应的值
                    String name = highlightField.getFragments()[0].string();
                    hotelDoc.setName(name);
                }
            }
            System.out.println(hotelDoc);
        }
    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
