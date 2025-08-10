package com.dangbun.global.aop;

public @interface CheckDutyInPlace {
    String dutyIdParam() default "dutyId";
    String placeIdParam() default "placeId";
}
