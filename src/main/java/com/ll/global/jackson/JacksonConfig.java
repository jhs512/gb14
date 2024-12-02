package com.ll.global.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.ll.domain.member.member.service.MemberService;
import com.ll.spring.annotation.Bean;
import com.ll.spring.annotation.Configuration;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JacksonConfig {
    private final MemberService memberService;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }

    @Bean ObjectWriter objectWriter(ObjectMapper objectMapper) {
        return objectMapper().writerWithDefaultPrettyPrinter();
    }
}
