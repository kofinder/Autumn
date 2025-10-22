package com.autumn.beans;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value();

    boolean required() default true;

    String defaultValue() default "";
}
