package com.autumn.web.mapping;

import com.autumn.beans.DeleteMapping;
import com.autumn.beans.GetMapping;
import com.autumn.beans.PatchMapping;
import com.autumn.beans.PostMapping;
import com.autumn.beans.PutMapping;
import com.autumn.web.HandlerMethod;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HandlerMapping {

    private final Map<String, HandlerMethod> handlers = new HashMap<>();
    private final Map<HandlerMethod, Pattern> pathPatterns = new HashMap<>();
    private final Map<HandlerMethod, List<String>> pathVariableNames = new HashMap<>();

    public void registerController(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            registerMethod(GetMapping.class, "GET", method, instance);
            registerMethod(PostMapping.class, "POST", method, instance);
            registerMethod(PutMapping.class, "PUT", method, instance);
            registerMethod(DeleteMapping.class, "DELETE", method, instance);
            registerMethod(PatchMapping.class, "PATCH", method, instance);
        }
    }

    public HandlerMethod getHandler(String httpMethod, String requestPath) {
        var handler = handlers.get(httpMethod + " " + requestPath);
        if (handler != null)
            return handler;

        for (var entry : pathPatterns.entrySet()) {
            var matcher = entry.getValue().matcher(requestPath);
            if (matcher.matches()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public Map<String, String> extractPathVariables(String requestPath, HandlerMethod handler) {
        Map<String, String> map = new HashMap<>();
        Pattern pattern = pathPatterns.get(handler);
        List<String> varNames = pathVariableNames.get(handler);
        if (pattern != null && varNames != null) {
            Matcher matcher = pattern.matcher(requestPath);
            if (matcher.matches()) {
                for (int i = 0; i < varNames.size(); i++) {
                    map.put(varNames.get(i), matcher.group(i + 1));
                }
            }
        }
        return map;
    }

    private <T extends java.lang.annotation.Annotation> void registerMethod(Class<T> annotationClass,
            String httpMethod,
            Method method,
            Object instance) {
        if (method.isAnnotationPresent(annotationClass)) {
            T annotation = method.getAnnotation(annotationClass);
            String path = null;
            try {
                path = (String) annotation.getClass().getMethod("value").invoke(annotation);
            } catch (Exception e) {
                e.printStackTrace();
            }

            HandlerMethod handler = new HandlerMethod(instance, method);
            handlers.put(httpMethod + " " + path, handler);

            List<String> variables = new ArrayList<>();
            Pattern p = Pattern.compile("\\{(\\w+)\\}");
            Matcher matcher = p.matcher(path);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                variables.add(matcher.group(1));
                matcher.appendReplacement(sb, "([^/]+)");
            }
            matcher.appendTail(sb);
            String regex = "^" + sb.toString() + "$";
            Pattern pattern = Pattern.compile(regex);

            pathPatterns.put(handler, pattern);
            pathVariableNames.put(handler, variables);
        }
    }

}
