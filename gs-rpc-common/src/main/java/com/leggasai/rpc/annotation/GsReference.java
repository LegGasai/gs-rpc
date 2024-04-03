package com.leggasai.rpc.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-19-21:48
 * @Description: 客户端RPC服务注解
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Component
public @interface GsReference {
    Class<?> interfaceClass() default void.class;

    String version() default "";


}
