package com.autumn;

import java.util.Set;

import com.autumn.beans.AutumnApplication;

public class AutumnApplicationRunner {

    public static void run(Class<?> mainClass) {
        if (!mainClass.isAnnotationPresent(AutumnApplication.class)) {
            throw new RuntimeException("Main class must be annotated with @AutumnApplication");
        }

        String basePackage = mainClass.getPackageName();
        Set<Class<?>> classes = PackageScanner.getClasses(basePackage);

        for (Class<?> clazz : classes) {
            if (AutumnBeanFactory.INSTANCE.isManagedBean(clazz)) {
                AutumnBeanFactory.INSTANCE.getInstanceOf(clazz);
            }
        }

        // try {
        // mainClass.getDeclaredMethod("main", String[].class)
        // .invoke(null, (Object) new String[] {});

        // } catch (Exception e) {
        // throw new RuntimeException("Failed to run main method", e);
        // }
    }
}
