package com.northeastern.info7225.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public class JsonValidation {

    public static boolean jsonValidate(String payload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode payloadJson = mapper.readTree(payload);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream schemaStream = inputStreamFromClasspath("schema/plan_entity.json");
        JsonSchema schema = schemaFactory.getSchema(schemaStream);

        Set<ValidationMessage> validationResult = schema.validate(payloadJson);

        if (validationResult.isEmpty()) {
            System.out.println("no validation errors :-)");
            return true;
        } else {
            validationResult.forEach(vm -> System.out.println(vm.getMessage()));
            return false;
        }
    }

    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }
}
