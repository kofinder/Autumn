package com.autumn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.autumn.beans.AsyncListener;
import com.autumn.beans.AutumnApplication;
import com.autumn.beans.ConditionalOnClass;
import com.autumn.beans.ConditionalOnMissingBean;
import com.autumn.beans.EventListener;
import com.autumn.beans.RetryableListener;
import com.autumn.event.ApplicationEvent;
import com.autumn.event.ContextRefreshedEvent;

public class AutumnApplicationContext {
    private boolean active = false;
    private final Map<Class<?>, List<EventListenerMethod>> eventListeners = new HashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AutumnBeanFactory beanFactory = AutumnBeanFactory.getInstance();

    public AutumnApplicationContext(Class<?> mainClass) {

        if (!mainClass.isAnnotationPresent(AutumnApplication.class)) {
            throw new RuntimeException("Main class must be annotated with @AutumnApplication");
        }

        refresh(mainClass);
    }

    public void refresh(Class<?> mainClass) {
        String basePackage = mainClass.getPackageName();
        Set<Class<?>> classes = PackageScanner.getClasses(basePackage);

        for (Class<?> clazz : classes) {

            if (clazz.isAnnotationPresent(ConditionalOnClass.class)) {
                String[] requiredClasses = clazz.getAnnotation(ConditionalOnClass.class)
                        .value();
                boolean allPresent = true;
                for (String className : requiredClasses) {
                    try {
                        Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        allPresent = false;
                        break;
                    }
                }
                if (!allPresent)
                    continue;
            }

            if (clazz.isAnnotationPresent(ConditionalOnMissingBean.class)) {
                Class<?>[] missingBeans = clazz.getAnnotation(ConditionalOnMissingBean.class)
                        .value();
                boolean exists = false;
                for (Class<?> beanType : missingBeans) {
                    if (beanFactory.containsBean(beanType)) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    System.out.println("⏭️ Skipping " + clazz.getSimpleName() + " (bean already exists)");
                    continue;
                }
            }

            if (beanFactory.isManagedBean(clazz)) {
                Object bean = beanFactory.getInstanceOf(clazz);
                registerEventListeners(bean, clazz);
            }
        }

        active = true;
        publishEvent(new ContextRefreshedEvent(this));
    }

    public <T> T getBean(Class<T> clazz) {
        return beanFactory.getInstanceOf(clazz);
    }

    public Set<Class<?>> getAllRegisteredBeans() {
        return new HashSet<>(beanFactory.getRegistry().keySet());
    }

    public void publishEvent(ApplicationEvent event) {
        List<EventListenerMethod> listeners = findMatchingListeners(event.getClass());
        for (EventListenerMethod listener : listeners) {
            if (listener.async) {
                executor.submit(() -> invokeWithRetry(listener, event));
            } else {
                invokeWithRetry(listener, event);
            }
        }
    }

    private void invokeWithRetry(EventListenerMethod listener, ApplicationEvent event) {
        int attempts = listener.retryAttempts;
        long delay = listener.retryDelay;

        for (int i = 1; i <= attempts; i++) {
            try {
                listener.method.invoke(listener.bean, event);
                return;
            } catch (Exception e) {
                System.err.println("⚠️  Listener failed (" + listener.method.getName() +
                        "), attempt " + i + " of " + attempts);
                if (i == attempts) {
                    System.err.println("❌ Giving up after " + attempts + " attempts.");
                    e.printStackTrace();
                } else {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    private List<EventListenerMethod> findMatchingListeners(Class<?> eventType) {
        List<EventListenerMethod> matching = new ArrayList<>();
        for (Map.Entry<Class<?>, List<EventListenerMethod>> entry : eventListeners.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventType)) {
                matching.addAll(entry.getValue());
            }
        }
        return matching;
    }

    private void registerEventListeners(Object bean, Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventListener.class)) {
                method.setAccessible(true);

                boolean isAsync = method.isAnnotationPresent(AsyncListener.class);
                RetryableListener retryable = method.getAnnotation(RetryableListener.class);

                int attempts = (retryable != null) ? retryable.attempts() : 1;
                long delay = (retryable != null) ? retryable.delay() : 0L;

                EventListener annotation = method.getAnnotation(EventListener.class);
                Class<?>[] eventTypes = annotation.value();
                if (eventTypes.length == 0 && method.getParameterCount() == 1) {
                    eventTypes = new Class<?>[] { method.getParameterTypes()[0] };
                }

                for (Class<?> eventType : eventTypes) {
                    eventListeners
                            .computeIfAbsent(eventType, k -> new ArrayList<>())
                            .add(new EventListenerMethod(bean, method, isAsync, attempts, delay));
                }
            }
        }
    }

    public void close() {
        active = false;
        executor.shutdownNow();
    }

    public boolean isActive() {
        return active;
    }

    private static class EventListenerMethod {
        final Object bean;
        final Method method;
        final boolean async;
        final int retryAttempts;
        final long retryDelay;

        EventListenerMethod(Object bean, Method method, boolean async, int retryAttempts, long retryDelay) {
            this.bean = bean;
            this.method = method;
            this.async = async;
            this.retryAttempts = retryAttempts;
            this.retryDelay = retryDelay;
        }
    }

}
