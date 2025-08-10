package com.dangbun.global.docs;

import com.dangbun.global.response.status.ResponseStatus;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentedApiErrors {
    Class<? extends Enum<? extends ResponseStatus>>[] value();

    String[] includes() default {};
}
