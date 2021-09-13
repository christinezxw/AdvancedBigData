package com.northeastern.info7225.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northeastern.info7225.entity.ES_OPERATIONS;
import com.northeastern.info7225.entity.EsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaConsumer {

    @Autowired
    private ElasticSearchConnector elasticSearchConnector;

    @KafkaListener(topics = {"PUT"})
    public void listenPutIndex(String message) {
        try {
            Map<String, Object> map = new ObjectMapper().readValue(message, HashMap.class);
            System.out.println("cunsume PUT: " + map.get("objectId"));
            System.out.println("cunsume PUT: " + map.get("parentId"));
            System.out.println("cunsume PUT: " + map.get("objectMap"));
            Map<String, Object> objectMap = (Map<String, Object>) map.get("objectMap");
            elasticSearchConnector.addIndex((String) map.get("objectId"), objectMap, (String) map.get("parentId"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = {"UPDATE"})
    public void listenUpdateIndex(String message) {
        try {
            Map<String, Object> map = new ObjectMapper().readValue(message, HashMap.class);
            System.out.println("cunsume UPDATE: " + map.get("objectId"));
            System.out.println("cunsume UPDATE: " + map.get("parentId"));
            System.out.println("cunsume UPDATE: " + map.get("objectMap"));
            Map<String, Object> objectMap = (Map<String, Object>) map.get("objectMap");
            elasticSearchConnector.addIndex((String) map.get("objectId"), objectMap, (String) map.get("parentId"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @KafkaListener(topics = {"DELETE"})
    public void listenDeleteIndex(String message) {
        try {
            Map<String, Object> map = new ObjectMapper().readValue(message, HashMap.class);
            System.out.println("cunsume DELETE: " + map.get("objectId"));
            elasticSearchConnector.deleteIndex((String) map.get("objectId"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
