package org.entropy.restclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.entropy.restclient.pojo.Hotel;
import org.entropy.restclient.pojo.HotelDoc;
import org.entropy.restclient.service.HotelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class ElasticsearchDocumentTests {

    private RestHighLevelClient client;
    @Autowired
    private HotelService hotelService;

    @Test
    void testAddDocument() throws IOException {
        // 查询数据库
        Hotel hotel = hotelService.getById(36934L);
        // 映射转换为文档类型
        HotelDoc hotelDoc = new HotelDoc(hotel);
        IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
        ObjectMapper mapper = new ObjectMapper();
        request.source(mapper.writeValueAsString(hotelDoc), XContentType.JSON);
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocumentById() throws IOException {
        GetRequest request = new GetRequest("hotel", "36934");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        ObjectMapper mapper = new ObjectMapper();
        HotelDoc hotelDoc = mapper.readValue(json, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void testUpdateDocumentById() throws IOException {
        UpdateRequest request = new UpdateRequest("hotel", "36934");
        request.doc(
                "price", 952,
                "starName", "四钻"
        );
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteDocumentById() throws IOException {
        DeleteRequest request = new DeleteRequest("hotel", "36934");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulk() throws IOException {
        BulkRequest request = new BulkRequest();
        ObjectMapper mapper = new ObjectMapper();
        hotelService.list().forEach(hotel -> {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            try {
                request.add(new IndexRequest("hotel")
                        .id(hotel.getId().toString())
                        .source(mapper.writeValueAsString(hotelDoc), XContentType.JSON));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        client.bulk(request, RequestOptions.DEFAULT);
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
