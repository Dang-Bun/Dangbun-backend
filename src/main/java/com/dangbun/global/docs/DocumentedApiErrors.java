package com.dangbun.global.docs;


import com.dangbun.global.response.status.ResponseStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentedApiErrors {
    Class<? extends Enum<? extends ResponseStatus>>[] value(); // 에러 enum 클래스
    String[] includes() default {}; // 포함할 에러 이름
}
