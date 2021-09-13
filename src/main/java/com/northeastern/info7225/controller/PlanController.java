package com.northeastern.info7225.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.northeastern.info7225.entity.PlanResponseDTO;
import com.northeastern.info7225.entity.TokenDTO;
import com.northeastern.info7225.service.ElasticSearchConnector;
import com.northeastern.info7225.service.PlanService;
import com.northeastern.info7225.utils.ETagUtil;
import com.northeastern.info7225.utils.JsonValidation;
import com.northeastern.info7225.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/plan")
public class PlanController {
    @Autowired
    private PlanService planService;

    @Autowired
    private ElasticSearchConnector elasticSearchConnector;

    @GetMapping("/{objectId}")
    public ResponseEntity<Object> getPlan(@PathVariable(name = "objectId", required = true) String objectId,
                                          @RequestHeader HttpHeaders requestHeaders) {
        // check authorization
        try {
            if (!JwtUtil.authorized(requestHeaders)) {
                return new ResponseEntity<>(new PlanResponseDTO("not authorized!"), HttpStatus.UNAUTHORIZED);
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("authorization error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            String plan = planService.getPlan(objectId);
            if (plan == null || plan.length() == 0) {
                return new ResponseEntity<>(new PlanResponseDTO("plan not found"), HttpStatus.NOT_FOUND);
            }
            String etag = ETagUtil.generateEtag(plan);
            return ResponseEntity.ok().eTag(etag).body(plan);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("getting plan error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping()
    public ResponseEntity<Object> postPlan(@RequestBody String payload,
                                           @RequestHeader HttpHeaders requestHeaders) {
        // check authorization
        try {
            if (!JwtUtil.authorized(requestHeaders)) {
                return new ResponseEntity<>(new PlanResponseDTO("not authorized!"), HttpStatus.UNAUTHORIZED);
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("authorization error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try {
            if (!JsonValidation.jsonValidate(payload)) {
                return new ResponseEntity<>(new PlanResponseDTO("json validation fail!"), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("json validation error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // save plan
        try {
            if (planService.savePlan(payload)) {
                Map<String, Object> payloadMap =
                        new ObjectMapper().readValue(payload, HashMap.class);
                String objectId = payloadMap.get("objectId").toString();
                // elastic search add index
//                elasticSearchConnector.addIndex(objectId, payload);
                // etag
                String savedPlan = planService.getPlan(objectId);
                String etag = ETagUtil.generateEtag(savedPlan);
                return ResponseEntity.ok().eTag(etag).body(new PlanResponseDTO("Success"));
            } else {
                return new ResponseEntity<>(new PlanResponseDTO("plan already exists!"), HttpStatus.BAD_REQUEST);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("post error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<Object> deletePlan(@PathVariable String objectId,
                                             @RequestHeader HttpHeaders requestHeaders) {
        // check authorization
        try {
            if (!JwtUtil.authorized(requestHeaders)) {
                return new ResponseEntity<>(new PlanResponseDTO("not authorized!"), HttpStatus.UNAUTHORIZED);
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("authorization error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (!planService.deletePlan(objectId)) {
            return new ResponseEntity<>(new PlanResponseDTO("plan not found"), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new PlanResponseDTO("Success"), HttpStatus.OK);
    }

    @PatchMapping("/{objectId}")
    public ResponseEntity<Object> patchPlan(@PathVariable(name = "objectId", required = true) String objectId,
                                            @RequestBody String payload,
                                            @RequestHeader HttpHeaders requestHeaders) {
        // check authorization
        try {
            if (!JwtUtil.authorized(requestHeaders)) {
                return new ResponseEntity<>(new PlanResponseDTO("not authorized!"), HttpStatus.UNAUTHORIZED);
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("authorization error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // check etag
        ResponseEntity<Object> etagResponse = checkEtag(requestHeaders, objectId);
        if (etagResponse != null) {
            return etagResponse;
        }
        // patch
        try {
            if (planService.patch(payload)) {
                String plan = planService.getPlan(objectId);
                String etag = ETagUtil.generateEtag(plan);
                return ResponseEntity.ok().eTag(etag).body(new PlanResponseDTO("Success"));
            } else {
                return new ResponseEntity<>(new PlanResponseDTO("plan not found"), HttpStatus.NOT_FOUND);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("patch error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{objectId}")
    public ResponseEntity<Object> putPlan(@PathVariable(name = "objectId", required = true) String objectId,
                                          @RequestBody String payload,
                                          @RequestHeader HttpHeaders requestHeaders) {
        // check authorization
        try {
            if (!JwtUtil.authorized(requestHeaders)) {
                return new ResponseEntity<>(new PlanResponseDTO("not authorized!"), HttpStatus.UNAUTHORIZED);
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("authorization error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // json validation
        try {
            if (!JsonValidation.jsonValidate(payload)) {
                return new ResponseEntity<>(new PlanResponseDTO("json validation fail!"), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("json validation error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // check etag
        ResponseEntity<Object> etagResponse = checkEtag(requestHeaders, objectId);
        if (etagResponse != null) {
            return etagResponse;
        }
        // update
        try {
            if (planService.updatePlan(payload)) {
                String etag = ETagUtil.generateEtag(payload);
                return ResponseEntity.ok().eTag(etag).body(new PlanResponseDTO("Success"));
            } else {
                return new ResponseEntity<>(new PlanResponseDTO("plan not found"), HttpStatus.NOT_FOUND);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("put error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping("/token")
    public ResponseEntity<TokenDTO> getToken() {
        String token = null;
        try {
            token = JwtUtil.generateToken();
        } catch (JOSEException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new TokenDTO("get token error!", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TokenDTO tokenDTO = new TokenDTO("Success", token);
        return new ResponseEntity<>(tokenDTO, HttpStatus.OK);

    }

    private ResponseEntity<Object> checkEtag(HttpHeaders requestHeaders, String id) {
        if (requestHeaders.getFirst("If-Match") == null) {
            return new ResponseEntity<>(new PlanResponseDTO("etag is needed!"), HttpStatus.FORBIDDEN);
        }
        String headerEtag = requestHeaders.getFirst("If-Match").split("\"")[1];

        try {
            String oldPlan = planService.getPlan(id);
            if (oldPlan == null) {
                return new ResponseEntity<>(new PlanResponseDTO("plan not found"), HttpStatus.NOT_FOUND);
            }
            String oldEtag = ETagUtil.generateEtag(oldPlan);
            if (!headerEtag.equals(oldEtag)) {
                return new ResponseEntity<>(new PlanResponseDTO("etag not match!"), HttpStatus.PRECONDITION_FAILED);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(new PlanResponseDTO("etag error!"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

}
