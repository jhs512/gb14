package com.ll.domain.post.post.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ll.spring.annotation.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ObjectMapper objectMapper;
}
