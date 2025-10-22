package com.autumn;

import java.util.Set;

import com.autumn.beans.AutumnApplication;

public class AutumnApplicationRunner {

    public static AutumnApplicationContext run(Class<?> mainClass, String... args) {
        if (!mainClass.isAnnotationPresent(AutumnApplication.class)) {
            throw new RuntimeException("Main class must be annotated with @AutumnApplication");
        }

        AutumnApplicationContext context = new AutumnApplicationContext(mainClass);

        runStartupHooks(context);

        return context;
    }

    private static void runStartupHooks(AutumnApplicationContext context) {
        Set<Class<?>> allBeans = context.getAllRegisteredBeans();
        for (Class<?> beanClass : allBeans) {
            Object bean = context.getBean(beanClass);
            if (bean instanceof ApplicationStartup startupBean) {
                System.out.println("▶ Running startup hook: " + beanClass.getSimpleName());
                startupBean.run();
            }
        }
    }
}
