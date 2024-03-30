package com.leggasai.rpc.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-19:09
 * @Description: 服务提供者属性配置
 */
@Configuration
public class ProviderProperties {

    @Value("${rpc.name}")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
