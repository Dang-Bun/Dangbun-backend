package com.dangbun.global.aop;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckDutyMembership {
    String dutyIdParam() default "dutyId";
    String placeIdParam() default ""; // 선택
}