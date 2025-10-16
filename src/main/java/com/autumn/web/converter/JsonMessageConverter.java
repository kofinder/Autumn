package com.autumn.web.converter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMessageConverter implements HttpMessageConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(String contentType) {
        return "application/json".equalsIgnoreCase(contentType);
    }

    @Override
    public Object read(String body, Class<?> targetType) {
        try {
            return objectMapper.readValue(body, targetType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

    @Override
    public String write(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }
}
