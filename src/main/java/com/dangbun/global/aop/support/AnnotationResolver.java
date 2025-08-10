package com.dangbun.global.aop.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
public final class AnnotationResolver {

    public <A extends Annotation> A resolve(ProceedingJoinPoint jp, Class<A> annType) {
        MethodSignature sig = (MethodSignature) jp.getSignature();
        Method proxyMethod = sig.getMethod();
        Class<?> targetClass = jp.getTarget().getClass();

        Method specificMethod = AopUtils.getMostSpecificMethod(proxyMethod, targetClass);
        A ann = AnnotatedElementUtils.findMergedAnnotation(specificMethod, annType);
        if (ann != null) return ann;

        ann = AnnotatedElementUtils.findMergedAnnotation(proxyMethod, annType);
        if (ann != null) return ann;


        return AnnotatedElementUtils.findMergedAnnotation(targetClass, annType);
    }
}