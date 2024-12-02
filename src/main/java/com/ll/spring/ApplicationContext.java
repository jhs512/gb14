package com.ll.spring;

import com.ll.domain.post.post.controller.ApiV1PostController.ApiV1PostController;

public class ApplicationContext {
    public ApplicationContext(String basePackage) {

    }

    public <T> T getBean(String beanName) {
        return (T) new ApiV1PostController();
    }
}
