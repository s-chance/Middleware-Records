package org.entropy.restclient.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.entropy.restclient.dto.RequestParams;
import org.entropy.restclient.mapper.HotelMapper;
import org.entropy.restclient.pojo.Hotel;
import org.entropy.restclient.pojo.HotelDoc;
import org.entropy.restclient.vo.PageResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> {
    private final RestHighLevelClient client;

    public HotelService(RestHighLevelClient client) {
        this.client = client;
    }

    public PageResult search(RequestParams params) {
        SearchRequest request = new SearchRequest("hotel");
        String key = params.getKey();
        if (key == null || key.isEmpty()) {
            request.source()
                    .query(QueryBuilders.matchAllQuery());
        } else {
            request.source()
                    .query(QueryBuilders.matchQuery("all", key));
        }
        int page = params.getPage();
        int size = params.getSize();
        request.source()
                .from((page - 1) * size)
                .size(size);

        SearchResponse search = null;
        try {
            search = client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return handleResponse(search);
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
                hotels.add(hotelDoc);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return new PageResult(total, hotels);
    }
}
