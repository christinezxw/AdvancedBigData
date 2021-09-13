package com.northeastern.info7225.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TokenDTO implements Serializable {
    private static final long serialVersionUID = 5039865445171014335L;
    private String message;
    private String token;

    public TokenDTO(String message, String token) {
        this.message = message;
        this.token = token;
    }
}
