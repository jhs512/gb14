package com.ll.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.ll.spring.annotation.Component;
import java.util.HashMap;
import java.util.List;
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
        createBeanDefinitions(candidates);
        registerBeans(candidates);
    }

    private Set<Class<?>> scanComponents() {
        Reflections reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        return reflections.getTypesAnnotatedWith(Component.class);
    }

    private void createBeanDefinitions(Set<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            if (isConcreteClass(candidate)) {
                String beanName = generateBeanName(candidate);
                registerBean(beanName, null); // 먼저 빈 이름만 등록
            }
        }
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
            Constructor<?> constructor = cls.getDeclaredConstructors()[0]; // RequiredArgsConstructor로 생성된 생성자 사용
            
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }

            // 생성자 파라미터에 맞는 빈들을 찾아서 주입
            List<Object> params = new ArrayList<>();
            for (Parameter parameter : constructor.getParameters()) {
                Class<?> parameterType = parameter.getType();
                Object dependency = findBeanByType(parameterType);
                if (dependency == null) {
                    throw new RuntimeException(
                        String.format("No bean found for parameter type: %s in class: %s", 
                            parameterType.getName(), 
                            cls.getName()
                        )
                    );
                }
                params.add(dependency);
            }

            return constructor.newInstance(params.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate class: " + cls.getName(), e);
        }
    }

    private Object findBeanByType(Class<?> type) {
        for (Object bean : beans.values()) {
            if (bean != null && type.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }

    private void registerBean(String beanName, Object instance) {
        beans.put(beanName, instance);
    }

    public <T> T getBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
