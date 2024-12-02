package com.ll.spring;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import com.ll.spring.annotation.Bean;
import com.ll.spring.annotation.Component;
import com.ll.spring.annotation.Configuration;

public class ApplicationContext {
    private final Map<String, Object> beans = new HashMap<>();
    private final String basePackage;
    private final Reflections reflections;

    public ApplicationContext(String basePackage) {
        this.basePackage = basePackage;
        this.reflections = new Reflections(basePackage, Scanners.TypesAnnotated);
        initialize();
    }

    private void initialize() {
        createConfigurationBeans();
        createComponentBeans();
    }

    private void createConfigurationBeans() {
        Set<Class<?>> configurations = findConfigurationClasses();
        configurations.forEach(this::processConfigurationClass);
    }

    private Set<Class<?>> findConfigurationClasses() {
        return reflections.getTypesAnnotatedWith(Configuration.class).stream()
                .filter(this::isInstantiableClass)
                .collect(Collectors.toSet());
    }

    private void processConfigurationClass(Class<?> configClass) {
        Object configInstance = createBean(configClass);
        processBeanMethods(configClass, configInstance);
    }

    private void processBeanMethods(Class<?> configClass, Object configInstance) {
        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Bean.class)) {
                createBeanFromMethod(method, configInstance);
            }
        }
    }

    private void createBeanFromMethod(Method method, Object configInstance) {
        try {
            String beanName = method.getName();
            
            // 메서드 파라미터의 의존성 해결
            Object[] params = resolveDependencies(method);
            
            Object beanInstance = method.invoke(configInstance, params);
            registerBean(beanName, beanInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean from method: " + method.getName(), e);
        }
    }

    private Object[] resolveDependencies(Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] params = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            params[i] = resolveDependency(parameters[i], method.getDeclaringClass());
        }

        return params;
    }

    private void createComponentBeans() {
        Set<Class<?>> components = findComponentClasses();
        components.forEach(this::createBean);
    }

    private Set<Class<?>> findComponentClasses() {
        return reflections.getTypesAnnotatedWith(Component.class).stream()
                .filter(this::isInstantiableClass)
                .filter(cls -> !cls.isAnnotationPresent(Configuration.class))
                .collect(Collectors.toSet());
    }

    private Object createBean(Class<?> cls) {
        String beanName = generateBeanName(cls);

        if (isExistingBean(beanName)) {
            return beans.get(beanName);
        }

        markBeanAsInProgress(beanName);

        try {
            Object instance = instantiateBean(cls);
            registerBean(beanName, instance);
            return instance;
        } catch (Exception e) {
            removeBeanInProgress(beanName);
            throw new RuntimeException("Failed to create bean: " + cls.getName(), e);
        }
    }

    private boolean isExistingBean(String beanName) {
        return beans.get(beanName) != null;
    }

    private void markBeanAsInProgress(String beanName) {
        beans.put(beanName, null);
    }

    private void removeBeanInProgress(String beanName) {
        beans.remove(beanName);
    }

    private Object instantiateBean(Class<?> cls) throws Exception {
        Constructor<?> constructor = cls.getDeclaredConstructors()[0];
        Object[] params = resolveDependencies(constructor);
        return constructor.newInstance(params);
    }

    private Object[] resolveDependencies(Constructor<?> constructor) {
        Parameter[] parameters = constructor.getParameters();
        Object[] params = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            params[i] = resolveDependency(parameters[i], constructor.getDeclaringClass());
        }

        return params;
    }

    private Object resolveDependency(Parameter parameter, Class<?> dependentClass) {
        Class<?> parameterType = parameter.getType();
        Object dependency = findBeanByType(parameterType);

        if (dependency == null) {
            dependency = createBeanOfType(parameterType);
        }

        if (dependency == null) {
            throw new RuntimeException(
                    String.format("No bean found for parameter type: %s in class: %s",
                            parameterType.getName(),
                            dependentClass.getName()
                    )
            );
        }

        return dependency;
    }

    private Object createBeanOfType(Class<?> type) {
        return findComponentClasses().stream()
                .filter(type::isAssignableFrom)
                .findFirst()
                .map(this::createBean)
                .orElse(null);
    }

    private boolean isInstantiableClass(Class<?> cls) {
        return !cls.isAnnotation() && !cls.isInterface();
    }

    private String generateBeanName(Class<?> cls) {
        String className = cls.getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private Object findBeanByType(Class<?> type) {
        return beans.values().stream()
                .filter(bean -> bean != null && type.isAssignableFrom(bean.getClass()))
                .findFirst()
                .orElse(null);
    }

    private void registerBean(String beanName, Object instance) {
        beans.put(beanName, instance);
    }

    public <T> T getBean(String beanName) {
        return (T) beans.get(beanName);
    }
}
