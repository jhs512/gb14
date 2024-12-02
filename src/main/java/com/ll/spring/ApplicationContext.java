package com.ll.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.ll.spring.annotation.RestController;
import com.ll.spring.annotation.Service;

public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();

    public ApplicationContext(String basePackage) {
        scan(basePackage);
    }

    private void scan(String basePackage) {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);

        // Component로 마크된 실제 구현 클래스들만 스캔
        Set<Class<?>> components = reflections.getTypesAnnotatedWith(RestController.class, true);

        // 스테레오타입 어노테이션들로 마크된 클래스들도 추가
        components.addAll(reflections.getTypesAnnotatedWith(Service.class, true));

        // 스캔된 컴포넌트들을 객체화하여 beans 맵에 저장
        for (Class<?> componentClass : components) {
            try {
                String beanName = componentClass.getSimpleName();
                // 첫 글자를 소문자로 변환하여 빈 이름 생성
                beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
                
                Object instance = componentClass.getDeclaredConstructor().newInstance();
                beans.put(beanName, instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public <T> T getBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
