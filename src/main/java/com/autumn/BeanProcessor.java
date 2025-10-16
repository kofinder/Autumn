package com.autumn;

public interface BeanProcessor {

    default Object postProcessBeforeInitialization(Object bean, Class<?> beanClass) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, Class<?> beanClass) {
        return bean;
    }

}
