package com.northeastern.info7225.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northeastern.info7225.entity.ES_OPERATIONS;
import com.northeastern.info7225.entity.EsMessage;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void producePutIndex(JSONObject message) {

        kafkaTemplate.send(ES_OPERATIONS.PUT.toString(), message.toString());
    }

    public void produceUpdateIndex(JSONObject message) {
        kafkaTemplate.send(ES_OPERATIONS.UPDATE.toString(), message.toString());
    }

    public void produceDeleteIndex(JSONObject message) {
        kafkaTemplate.send(ES_OPERATIONS.DELETE.toString(), message.toString());
    }
}
