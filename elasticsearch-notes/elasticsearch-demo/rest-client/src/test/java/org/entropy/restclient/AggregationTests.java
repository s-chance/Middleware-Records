package org.entropy.restclient;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class AggregationTests {

    private RestHighLevelClient client;

    @Test
    void testAggregation() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .size(0)
                .aggregation(AggregationBuilders
                        .terms("brandAgg")
                        .field("brand")
                        .size(20));
        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        // 解析聚合结果
        Aggregations aggregations = search.getAggregations();
        // 根据名称获取聚合结果
        Terms brandTerms = aggregations.get("brandAgg");
        // 获取桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            String brandName = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            System.out.println(brandName + ": " + docCount);
        }
    }

    @Test
    void testSuggest() throws IOException {
        SearchRequest request = new SearchRequest("hotel");
        request.source()
                .suggest(new SuggestBuilder().addSuggestion(
                        "suggestions",
                        SuggestBuilders.completionSuggestion("suggestion")
                                .prefix("sd")
                                .skipDuplicates(true)
                                .size(10)
                ));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        Suggest suggest = response.getSuggest();
        // 根据名称获取补全结果
        CompletionSuggestion suggestions = suggest.getSuggestion("suggestions");
        suggestions.getOptions().forEach(option -> System.out.println(option.getText().string()));
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
