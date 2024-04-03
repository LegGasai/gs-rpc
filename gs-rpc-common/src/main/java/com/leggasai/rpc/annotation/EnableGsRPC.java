package com.leggasai.rpc.annotation;


import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-19-21:48
 * @Description: 开启GS RPC,这样springboot会自动扫描所有com.leggasai.rpc的Bean
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(GsScanConfig.class)
public @interface EnableGsRPC {
}
