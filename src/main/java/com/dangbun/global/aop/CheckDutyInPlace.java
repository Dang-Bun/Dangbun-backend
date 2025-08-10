package com.dangbun.global.aop;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckDutyInPlace {
    String dutyIdParam() default "dutyId";
    String placeIdParam() default "placeId";
}
