package com.ll.global.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        // 잭슨 객체생성
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper;
    }
}
