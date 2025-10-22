package com.autumn.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandlerMethod {

    private final Object controllerInstance;
    private final Method method;

    public HandlerMethod(Object controllerInstance, Method method) {
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    public Object getControllerInstance() {
        return controllerInstance;
    }

    public Method getMethod() {
        return method;
    }

    public Parameter[] getParameters() {
        return method.getParameters();
    }

    public Object invoke(Object... args)
            throws IllegalAccessException, InvocationTargetException {
        return method.invoke(controllerInstance, args);
    }
}
