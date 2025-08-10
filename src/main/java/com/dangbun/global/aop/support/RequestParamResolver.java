package com.dangbun.global.aop.support;


import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public final class RequestParamResolver {


    public Long resolveLong(JoinPoint jp, String name) {
        // 1) PathVariable
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            Object vars = req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (vars instanceof Map<?, ?> map) {
                Object v = map.get(name);
                if (v != null) {
                    try { return Long.valueOf(v.toString()); } catch (NumberFormatException ignored) {}
                }
            }
            // 2) QueryParam
            String qp = req.getParameter(name);
            if (qp != null) {
                try { return Long.valueOf(qp); } catch (NumberFormatException ignored) {}
            }
        }

        // 3) Method Parameter
        Signature sig = jp.getSignature();
        if (sig instanceof MethodSignature ms) {
            String[] names = ms.getParameterNames();
            Object[] args = jp.getArgs();
            if (names != null) {
                for (int i = 0; i < names.length; i++) {
                    if (name.equals(names[i]) && args[i] != null) {
                        Object a = args[i];
                        if (a instanceof Long l) return l;
                        if (a instanceof Number n) return n.longValue();
                        try { return Long.valueOf(a.toString()); } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return null;
    }
}