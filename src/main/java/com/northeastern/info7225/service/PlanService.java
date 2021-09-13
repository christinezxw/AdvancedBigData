package com.northeastern.info7225.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northeastern.info7225.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PlanService {
    @Autowired
    private PlanRepository repository;
    private static final String SEP = "____";


    public boolean savePlan(String payload) throws JsonProcessingException {
        return repository.save(payload);
    }

    public boolean updatePlan(String payload) throws JsonProcessingException {
        return repository.update(payload);
    }


    public String getPlan(String objectId) throws JsonProcessingException {
        if (!objectExist("plan" + SEP + objectId)) {
            return null;
        }
        return repository.findById("plan" + SEP + objectId);
    }

    public boolean deletePlan(String objectId) {
        if (!objectExist("plan" + SEP + objectId)) {
            return false;
        }
        return repository.delete("plan", objectId);
    }


    public boolean patch(String payload) throws JsonProcessingException {
        Map<String, Object> payloadMap =
                new ObjectMapper().readValue(payload, HashMap.class);
        String objectId = payloadMap.get("objectId").toString();
        String objectType = payloadMap.get("objectType").toString();
        if (!objectExist(objectType + SEP + objectId)) {
            return false;
        } else {
            return repository.patch(payloadMap);
        }
    }

    private boolean objectExist(String key) {
        return repository.exist(key);
    }
}
