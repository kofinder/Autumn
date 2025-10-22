package com.autumn.beans;

import java.lang.annotation.*;

import com.autumn.event.ApplicationEvent;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventListener {
    Class<? extends ApplicationEvent>[] value() default {};
}
