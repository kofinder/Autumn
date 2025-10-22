package com.autumn.beans;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryableListener {

    int attempts() default 3; // retrieve count

    long delay() default 1000L; // delay milliseconds

}