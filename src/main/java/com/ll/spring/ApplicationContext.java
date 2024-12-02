package com.ll.spring;

import com.ll.domain.post.post.controller.ApiV1PostController.ApiV1PostController;
import com.ll.domain.post.post.service.PostService;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String basePackage) {
        scan(basePackage);
    }

    private void scan(String basePackage) {
        beans.put("apiV1PostController", new ApiV1PostController());
        beans.put("postService", new PostService());
    }

    public <T> T getBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
