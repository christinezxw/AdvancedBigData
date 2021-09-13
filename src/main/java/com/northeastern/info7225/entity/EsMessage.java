package com.northeastern.info7225.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class EsMessage implements Serializable {
    private static final long serialVersionUID = -512659726823836596L;
    private String id;
    private Map<String, Object> document;
    private String parentId;

    public EsMessage(String id) {
        this.id = id;
    }

    public EsMessage(String id, Map<String, Object> document, String parentId) {
        this.id = id;
        this.document = document;
        this.parentId = parentId;
    }
}
