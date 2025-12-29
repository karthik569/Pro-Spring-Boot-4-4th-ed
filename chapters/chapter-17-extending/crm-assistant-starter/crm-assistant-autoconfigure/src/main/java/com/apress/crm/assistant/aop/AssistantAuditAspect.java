package com.apress.crm.assistant.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect for auditing CRM Assistant interactions.
 *
 * This aspect automatically logs all method calls to the CrmAssistant interface,
 * including:
 * - Method name and arguments
 * - Execution time
 * - Return value
 *
 * This is a cross-cutting concern that's transparently added to user code
 * without them having to write any audit logging themselves.
 *
 * Enabled by setting: crm.assistant.audit.enabled=true
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "crm.assistant.audit", name = "enabled", havingValue = "true")
public class AssistantAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AssistantAuditAspect.class);

    /**
     * Intercepts all method calls to CrmAssistant implementations.
     *
     * The pointcut expression matches:
     * - Any method (* ...)
     * - In any class that implements CrmAssistant (com.apress.crm.assistant.CrmAssistant.*)
     * - With any parameters ((...))
     */
    @Around("execution(* com.apress.crm.assistant.CrmAssistant.*(..))")
    public Object auditAssistantCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("CRM Assistant - Call started: {} with args: {}", methodName, formatArgs(args));

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("CRM Assistant - Call failed: {} with error: {}", methodName, e.getMessage());
            throw e;
        }

        long endTime = System.currentTimeMillis();
        log.info("CRM Assistant - Call finished: {} in {}ms. Result: {}",
                methodName, (endTime - startTime), truncate(String.valueOf(result), 100));

        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(truncate(String.valueOf(args[i]), 50));
        }
        sb.append("]");
        return sb.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "null";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
    }
}
