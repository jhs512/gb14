package com.ll.spring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppTest {
    @Test
    @DisplayName("Test")
    public void t1() {
        ApplicationContext applicationContext = new ApplicationContext("com.ll");
    }
}
