package com.ll.spring;

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
}
