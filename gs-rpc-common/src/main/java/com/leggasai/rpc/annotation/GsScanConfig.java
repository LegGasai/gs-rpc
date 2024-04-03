package com.leggasai.rpc.annotation;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-19-21:48
 * @Description: 配置包扫描路径
 */
@ComponentScan(basePackages = "com.leggasai.rpc")
@Configuration
public @interface GsScanConfig {
}
