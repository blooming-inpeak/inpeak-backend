package com.blooming.inpeak.common.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.blooming.inpeak..service..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        logger.info("[시작] 메서드: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            logger.info("[종료] 메서드: {} | 실행 시간: {}ms", methodName, elapsedTime);
            return result;
        } catch (Exception e) {
            logger.error("[오류] 메서드: {} | 예외 발생: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
