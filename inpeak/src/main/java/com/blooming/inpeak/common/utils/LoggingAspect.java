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
    private static final int MAX_STRING_LENGTH = 200; // 너무 긴 문자열은 생략

    @Around("execution(* com.blooming.inpeak..service..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Object[] originalArgs = joinPoint.getArgs();

        Object[] loggableArgs = Arrays.stream(originalArgs)
            .map(arg -> {
                if (arg instanceof String str && str.length() > MAX_STRING_LENGTH) {
                    return "[내용 생략: 너무 긴 문자열]";
                }
                return arg;
            }).toArray();

        logger.info("[시작] 메서드: {} | 입력값: {}", methodName, loggableArgs);

        try {
            Object result = joinPoint.proceed();
            long elapsedTime = System.currentTimeMillis() - start;

            logger.info("[종료] 메서드: {} | 실행 시간: {}ms | 반환값: {}", methodName, elapsedTime, result);
            return result;
        } catch (Exception e) {
            logger.error("[오류] 메서드: {} | 예외 발생: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
