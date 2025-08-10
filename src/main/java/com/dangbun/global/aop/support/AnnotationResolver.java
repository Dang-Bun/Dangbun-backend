package com.dangbun.global.aop.support;

import org.aspectj.lang.ProceedingJoinPoint;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public final class AnnotationResolver {

    public <A extends Annotation> A resolve(ProceedingJoinPoint jp, Class<A> annType) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        A ann = sig.getMethod().getAnnotation(annType);
        if (ann != null) return ann;

        return jp.getTarget().getClass().getAnnotation(annType);
    }
}