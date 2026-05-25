package com.ecommerce.detail.ai.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 日志切面
 * 记录Controller层的方法调用日志
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 定义切入点：拦截所有Controller层的方法
     */
    @Pointcut("execution(* com.ecommerce.detail.ai.controller..*(..))")
    public void controllerPointcut() {
    }

    /**
     * 前置通知：在方法执行前记录请求信息
     */
    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info(">>> 开始执行: {}.{}(), 参数: {}", className, methodName, Arrays.toString(args));
    }

    /**
     * 后置通知：在方法执行后记录响应信息
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.info("<<< 执行完成: {}.{}(), 结果: {}", className, methodName, result);
    }

    /**
     * 异常通知：捕获方法执行中的异常
     */
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.error("!!! 执行异常: {}.{}(), 异常: {}", className, methodName, exception.getMessage(), exception);
    }

    /**
     * 环绕通知：记录方法执行时间
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            if (executionTime > 1000) {
                log.warn("⚠️ 方法执行耗时较长: {}.{}() 耗时: {}ms", className, methodName, executionTime);
            } else {
                log.debug("✓ 方法执行成功: {}.{}() 耗时: {}ms", className, methodName, executionTime);
            }
            
            return result;
        } catch (Throwable e) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            
            log.error("✗ 方法执行失败: {}.{}() 耗时: {}ms, 异常: {}", 
                     className, methodName, executionTime, e.getMessage());
            
            throw e;
        }
    }
}
