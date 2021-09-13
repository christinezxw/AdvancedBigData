package com.northeastern.info7225.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northeastern.info7225.entity.EsMessage;
import com.northeastern.info7225.service.ElasticSearchConnector;
import com.northeastern.info7225.service.KafkaProducer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class PlanRepository {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private KafkaProducer producer;

//    @Autowired
//    private ElasticSearchConnector elasticSearchConnector;

    private static final String SEP = "____";

    public boolean save(String payload) throws JsonProcessingException {
        Map<String, Object> payloadMap =
                new ObjectMapper().readValue(payload, HashMap.class);
        String objectId = payloadMap.get("objectId").toString();
        String objectType = payloadMap.get("objectType").toString();
        if (exist(objectType + SEP + objectId)) {
            return false;
        } else {
            savePlanHelper(objectType, objectId, payloadMap, "", "");
            System.out.println("saved");
            return true;
        }
    }

    public boolean update(String payload) throws JsonProcessingException {
        Map<String, Object> payloadMap =
                new ObjectMapper().readValue(payload, HashMap.class);
        String objectId = payloadMap.get("objectId").toString();
        String objectType = payloadMap.get("objectType").toString();
        String key = objectType + SEP + objectId;
        if (!exist(key)) {
            return false;
        } else {
            savePlanHelper(objectType, objectId, payloadMap, "", "");
            return true;
        }
    }

    private void savePlanHelper(String objectType, String objectId, Map<String, Object> payloadMap,
                                String parentId, String parentType) {
        String objectKey = objectType + SEP + objectId;
        Map<String, Object> simpleMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                Map<String, Object> childMap = (Map) entryValue;
                String childType = childMap.get("objectType").toString();
                String childId = childMap.get("objectId").toString();
                String childKey = childType + SEP + childId;
                String linkKey = objectKey + SEP + entryKey;
                saveSet(linkKey, childKey);
                savePlanHelper(childType, childId, childMap, objectId, objectType);
            } else if (entryValue instanceof List) {
                List<Map<String, Object>> list = (List) entryValue;
                String linkKey = objectKey + SEP + entryKey;
                for (Map<String, Object> childMap : list) {
                    String childType = childMap.get("objectType").toString();
                    String childId = childMap.get("objectId").toString();
                    String childKey = childType + SEP + childId;
                    saveSet(linkKey, childKey);
                    savePlanHelper(childType, childId, childMap, objectId, objectType);
                }
            } else {
                simpleMap.put(entryKey, String.valueOf(entryValue));
            }
        }
        saveMap(objectKey, simpleMap);
        // save to ES
        Map<String, String> mapping = new HashMap<>();
        if (parentId.length() != 0) {
            mapping.put("parent", parentId);
        }
        if (parentType.length() != 0) {
            mapping.put("name", parentType + "_" + objectType);
        } else {
            mapping.put("name", objectType);
        }
        simpleMap.put("plan_service", mapping);
        // TODO change into kafka
        JSONObject esMessage = new JSONObject();
        esMessage.put("objectId", objectId);
        esMessage.put("parentId", parentId);
        esMessage.put("objectMap", simpleMap);

        producer.producePutIndex(esMessage);
//        elasticSearchConnector.addIndex(objectId, simpleMap, parentId);
    }

    public void saveMap(String key, Map<String, Object> content) {
        redisTemplate.opsForHash().putAll(key, content);
    }

    public void saveSet(String key, String content) {
        redisTemplate.opsForSet().add(key, content);
    }

    public String findById(String key) throws JsonProcessingException {
        Map<String, Object> payloadMap = getHelper(key);
        return new ObjectMapper().writeValueAsString(payloadMap);
    }

    public Map<String, Object> getHelper(String id) {//id: type + SEP + *
        Map<String, Object> obj = new HashMap<>();
        Set<String> keys = redisTemplate.keys(id + SEP + "*");
        for (String key : keys) {//key: type + SEP + *
            Set<Object> childIds = redisTemplate.opsForSet().members(key);//
            if (childIds.size() > 1) {
                List<Object> list = new ArrayList<>();
                for (Object childId : childIds) {
                    list.add(getHelper(childId.toString()));
                }
                obj.put(key.split(SEP)[2], list);
            } else {
                for (Object childId : childIds) {
                    obj.put(key.split(SEP)[2], getHelper(childId.toString()));
                }
            }
        }

        // simple members
        HashOperations<String, String, String> hps = redisTemplate.opsForHash();
        Map<String, String> simpleMap = hps.entries(id);
        for (String simpleKey : simpleMap.keySet()) {
            obj.put(simpleKey, simpleMap.get(simpleKey));
        }
        return obj;

    }

    public boolean delete(String objectType, String objectId) {
        return deleteHelper(objectType, objectId);
    }

    private boolean deleteHelper(String objectType, String objectId) {
        String objectKey = objectType + SEP + objectId;
        Set<String> keys = redisTemplate.keys(objectKey + SEP + "*");
        for (String key : keys) {
            Set<Object> childKeys = redisTemplate.opsForSet().members(key);
            for (Object childKey : childKeys) {
                String[] childFields = String.valueOf(childKey).split(SEP);
                String childType = childFields[0];
                String childId = childFields[1];
                deleteHelper(childType, childId);
            }
            redisTemplate.delete(key);
        }

        // deleting simple fields
        redisTemplate.delete(objectKey);
        // TODO change into kafka
        JSONObject esMessage = new JSONObject();
        esMessage.put("objectId", objectId);
        producer.produceDeleteIndex(esMessage);
//        elasticSearchConnector.deleteIndex(objectId);
        return true;
    }

    public boolean exist(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean patch(Map<String, Object> content) {
        String objectId = content.get("objectId").toString();
        String objectType = content.get("objectType").toString();
        String id = objectType + SEP + objectId;
        HashOperations<String, String, Object> hps = redisTemplate.opsForHash();
        Map<String, Object> simpleMap = hps.entries(id);
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            String entryKey = entry.getKey();
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                Map<String, Object> childMap = (Map) entryValue;
                String childType = childMap.get("objectType").toString();
                String childId = childMap.get("objectId").toString();
                String childKey = childType + SEP + childId;
                String linkId = id + SEP + entryKey;
                saveSet(linkId, childKey);
                savePlanHelper(childType, childId, childMap, objectId, objectType);
            } else if (entryValue instanceof List) {
                List<Map<String, Object>> list = (List) entryValue;
                String linkId = id + SEP + entryKey;
                for (Map<String, Object> childMap : list) {
                    String childType = childMap.get("objectType").toString();
                    String childId = childMap.get("objectId").toString();
                    String childKey = childType + SEP + childId;
                    saveSet(linkId, childKey);
                    savePlanHelper(childType, childId, childMap, objectId, objectType);
                }
            } else {
                simpleMap.put(entryKey, String.valueOf(entryValue));
            }
        }
        saveMap(id, simpleMap);
        return true;
    }

}
