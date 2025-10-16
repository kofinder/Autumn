package com.autumn.web.converter;

public interface HttpMessageConverter {
    boolean supports(String contentType);

    Object read(String body, Class<?> targetType);

    String write(Object object);
}
