package com.dangbun.global.aop.support;

import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

public final class AnnotationResolver {

    private AnnotationResolver() {}

    public static <A extends Annotation> A resolve(ProceedingJoinPoint jp, Class<A> annType) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        A ann = sig.getMethod().getAnnotation(annType);
        if (ann != null) return ann;

        return jp.getTarget().getClass().getAnnotation(annType);
    }
}