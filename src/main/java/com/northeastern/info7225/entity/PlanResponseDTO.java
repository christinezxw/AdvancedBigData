package com.northeastern.info7225.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlanResponseDTO implements Serializable {
    private static final long serialVersionUID = -338094873154211112L;
    String message;

    public PlanResponseDTO(String message) {
        this.message = message;
    }
}
