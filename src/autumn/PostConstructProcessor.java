package autumn;

import java.lang.reflect.Method;

import autumn.beans.PostConstruct;

public class PostConstructProcessor implements BeanProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
