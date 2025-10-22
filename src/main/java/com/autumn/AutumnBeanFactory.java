package com.autumn;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.autumn.beans.Autowired;
import com.autumn.beans.Component;
import com.autumn.beans.Controller;
import com.autumn.beans.PostConstruct;
import com.autumn.beans.Repository;
import com.autumn.beans.Service;

public enum AutumnBeanFactory {

    INSTANCE;

    private final Map<Class<?>, Object> registry = new ConcurrentHashMap<>();
    private final List<BeanProcessor> processors = new ArrayList<>();

    public static AutumnBeanFactory getInstance() {
        return INSTANCE;
    }

    public void addBeanProcessor(BeanProcessor processor) {
        processors.add(processor);
    }

    public boolean isManagedBean(Class<?> beanClass) {
        return beanClass.isAnnotationPresent(Controller.class)
                || beanClass.isAnnotationPresent(Component.class)
                || beanClass.isAnnotationPresent(Service.class)
                || beanClass.isAnnotationPresent(Repository.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstanceOf(Class<T> beanClass, Object... arguments) {
        try {
            T bean;

            if (isManagedBean(beanClass)) {
                if (registry.containsKey(beanClass)) {
                    return (T) registry.get(beanClass);
                }

                bean = instantiateBeanClass(beanClass, arguments);

                for (BeanProcessor processor : processors) {
                    bean = (T) processor.postProcessBeforeInitialization(bean, beanClass);
                }

                registry.put(beanClass, bean);

                for (BeanProcessor processor : processors) {
                    bean = (T) processor.postProcessAfterInitialization(bean, beanClass);
                }

                registry.put(beanClass, bean);
            } else {
                bean = instantiateBeanClass(beanClass, arguments);
                for (BeanProcessor processor : processors) {
                    bean = (T) processor.postProcessAfterInitialization(bean, beanClass);
                }
            }

            return bean;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + beanClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T instantiateBeanClass(Class<T> beanClass, Object[] arguments)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        T bean;

        // Constructor Injection
        Constructor<?> autowiredConstructor = Arrays.stream(beanClass.getDeclaredConstructors())
                .filter(c -> c.isAnnotationPresent(Autowired.class))
                .findFirst().orElse(null);

        if (autowiredConstructor != null) {
            Class<?>[] paramTypes = autowiredConstructor.getParameterTypes();
            Object[] params = Arrays.stream(paramTypes)
                    .map(this::getInstanceOf)
                    .toArray();
            autowiredConstructor.setAccessible(true);
            bean = (T) autowiredConstructor.newInstance(params);
        } else {
            Constructor<T> constructor;
            if (arguments.length == 0) {
                constructor = beanClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                bean = constructor.newInstance();
            } else {
                Class<?>[] argumentClasses = Arrays.stream(arguments)
                        .map(Object::getClass)
                        .toArray(Class<?>[]::new);
                constructor = beanClass.getConstructor(argumentClasses);
                bean = constructor.newInstance(arguments);
            }
        }

        // Field Injection
        for (Field field : beanClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Object dependency = getInstanceOf(field.getType());
                field.setAccessible(true);
                field.set(bean, dependency);
            }
        }

        // PostConstruct
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                method.invoke(bean);
            }
        }

        return bean;
    }

    public Map<Class<?>, Object> getRegistry() {
        return registry;
    }

    public boolean containsBean(Class<?> beanClass) {
        return registry.containsKey(beanClass);
    }

}
