package com.northeastern.info7225.service;

import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticSearchConnector {

    public void addIndex(String id, Map<String, Object> document, String parentId) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http"),
                            new HttpHost("localhost", 9201, "http")));
            if (!documentExist(id)) {
                IndexRequest indexRequest = new IndexRequest("posts")
                        .index("plan")
                        .type("_doc")
                        .id(id)
                        .source(document)
                        .routing(parentId);
                client.index(indexRequest, RequestOptions.DEFAULT);
            } else {
                UpdateRequest updateRequest = new UpdateRequest()
                        .index("plan")
                        .type("_doc")
                        .id(id)
                        .doc(document)
                        .routing(parentId);
                client.update(updateRequest, RequestOptions.DEFAULT);
            }

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteIndex(String id) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("localhost", 9200, "http"),
                            new HttpHost("localhost", 9201, "http")));

            DeleteRequest request = new DeleteRequest()
                    .index("plan")
                    .type("_doc")
                    .id(id);
            client.delete(request, RequestOptions.DEFAULT);
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean documentExist(String id) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http")));
        GetRequest request = new GetRequest().index("plan").type("_doc").id(id);
        try {
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
