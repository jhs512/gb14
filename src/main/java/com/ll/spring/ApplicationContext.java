package com.ll.spring;

import com.ll.spring.annotation.Component;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();
    private final String basePackage;

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
        initialize();
    }

    private void initialize() {
        Set<Class<?>> candidates = scanComponents();
        registerBeans(candidates);
    }

    private Set<Class<?>> scanComponents() {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        return reflections.getTypesAnnotatedWith(Component.class);
    }

    private void registerBeans(Set<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            if (isConcreteClass(candidate)) {
                String beanName = generateBeanName(candidate);
                Object instance = instantiateClass(candidate);
                registerBean(beanName, instance);
            }
        }
    }

    private boolean isConcreteClass(Class<?> cls) {
        return !cls.isAnnotation() && !cls.isInterface();
    }

    private String generateBeanName(Class<?> cls) {
        String className = cls.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private Object instantiateClass(Class<?> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class: " + cls.getName(), e);
        }
    }

    private void registerBean(String beanName, Object instance) {
        beans.put(beanName, instance);
    }

    public <T> T getBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
