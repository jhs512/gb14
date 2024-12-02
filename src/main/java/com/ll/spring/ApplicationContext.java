package com.ll.spring;

import com.ll.spring.annotation.Component;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        return reflections.getTypesAnnotatedWith(Component.class).stream()
                .filter(cls -> !cls.isAnnotation() && !cls.isInterface())
                .collect(Collectors.toSet());
    }

    private void createBeanDefinitions(Set<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            String beanName = generateBeanName(candidate);
            registerBean(beanName, null); // 먼저 빈 이름만 등록
        }
    }

    private void registerBeans(Set<Class<?>> candidates) {
        for (Class<?> candidate : candidates) {
            createBean(candidate);
        }
    }

    private Object createBean(Class<?> cls) {
        String beanName = generateBeanName(cls);

        // 이미 생성된 빈이 있다면 반환
        Object existingBean = beans.get(beanName);
        if (existingBean != null) {
            return existingBean;
        }

        // 생성 진행 중임을 표시 (순환 참조 감지용)
        beans.put(beanName, null);

        try {
            Constructor<?> constructor = cls.getDeclaredConstructors()[0];
            Object[] params = new Object[constructor.getParameterCount()];

            // 생성자 파라미터의 의존성을 먼저 해결
            for (int i = 0; i < constructor.getParameterCount(); i++) {
                Parameter parameter = constructor.getParameters()[i];
                Class<?> parameterType = parameter.getType();

                // 의존성 빈을 찾거나 생성
                Object dependency = findBeanByType(parameterType);
                if (dependency == null) {
                    // 의존성 빈이 없다면 해당 타입의 빈을 찾아서 생성
                    dependency = createBeanOfType(parameterType);
                }

                if (dependency == null) {
                    throw new RuntimeException(
                            String.format("No bean found for parameter type: %s in class: %s",
                                    parameterType.getName(),
                                    cls.getName()
                            )
                    );
                }

                params[i] = dependency;
            }

            // 모든 의존성이 해결되면 인스턴스 생성
            Object instance = constructor.newInstance(params);
            beans.put(beanName, instance);
            return instance;

        } catch (Exception e) {
            beans.remove(beanName); // 실패시 생성 진행 중 표시 제거
            throw new RuntimeException("Failed to create bean: " + cls.getName(), e);
        }
    }

    private Object createBeanOfType(Class<?> type) {
        // 해당 타입의 구현체를 찾아서 생성
        for (Class<?> candidate : scanComponents()) {
            if (type.isAssignableFrom(candidate)) {
                return createBean(candidate);
            }
        }
        return null;
    }

    private String generateBeanName(Class<?> cls) {
        String className = cls.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
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
