package com.autumn.web.mapping;

import com.autumn.AutumnBeanFactory;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerMapping {
    private final Map<String, Method> getMappings = new HashMap<>();
    private final Map<String, Method> postMappings = new HashMap<>();
    private final Map<Class<?>, Object> controllerInstances = new HashMap<>();

    public void registerController(Class<?> controllerClass) {
        Object controller = AutumnBeanFactory.getInstance().getInstanceOf(controllerClass);
        controllerInstances.put(controllerClass, controller);

        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GetMapping.class)) {
                String path = method.getAnnotation(GetMapping.class).value();
                getMappings.put(path, method);
            } else if (method.isAnnotationPresent(PostMapping.class)) {
                String path = method.getAnnotation(PostMapping.class).value();
                postMappings.put(path, method);
            }
        }
    }

    public Method getHandler(String method, String path) {
        return "GET".equals(method) ? getMappings.get(path) : postMappings.get(path);
    }

    public Object getControllerInstance(Class<?> clazz) {
        return controllerInstances.get(clazz);
    }
}
