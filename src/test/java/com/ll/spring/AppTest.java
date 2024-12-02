package com.ll.spring;

import com.ll.domain.post.post.controller.ApiV1PostController.ApiV1PostController;
import com.ll.domain.post.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {
    @Test
    @DisplayName("applicationContext")
    public void t1() {
        ApplicationContext applicationContext = new ApplicationContext("com.ll");
    }

    @Test
    @DisplayName("apiV1PostController")
    public void t2() {
        ApplicationContext applicationContext = new ApplicationContext("com.ll");

        ApiV1PostController apiV1PostController = applicationContext.getBean("apiV1PostController");
    }

    @Test
    @DisplayName("postService")
    public void t3() {
        ApplicationContext applicationContext = new ApplicationContext("com.ll");

        PostService postService = applicationContext.getBean("postService");
    }

    @Test
    @DisplayName("objectMapper")
    public void t4() {
        ApplicationContext applicationContext = new ApplicationContext("com.ll");

        PostService postService = applicationContext.getBean("postService");
    }
}
