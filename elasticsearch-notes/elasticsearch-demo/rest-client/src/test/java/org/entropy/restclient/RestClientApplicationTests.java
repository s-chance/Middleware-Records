package org.entropy.restclient;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.entropy.restclient.constant.HotelConstant.MAPPING_TEMPLATE;

public class RestClientApplicationTests {

    private RestHighLevelClient client;

    @Test
    void testInit() {
        System.out.println(client);
    }

    @Test
    void testCreateIndex() throws IOException {
        // 创建request对象
        CreateIndexRequest request = new CreateIndexRequest("hotel");
        // 请求参数：MAPPING_TEMPLATE是静态常量字符串，内容就是创建索引库的DSL语句
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        // 发起请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("hotel");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testExistsIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("hotel");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
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
