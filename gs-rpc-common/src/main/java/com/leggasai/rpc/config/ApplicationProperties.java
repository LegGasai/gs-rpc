package com.leggasai.rpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-19:09
 * @Description: 应用配置
 */
@Configuration
public class ApplicationProperties {
    @Value("${gsrpc.application.name:}")
    private String name;
    @Value("${gsrpc.application.proxy:jdk}")
    private String proxy;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    /**
     * just for test, fixme
     * @return
     */
    @Override
    public String toString() {
        return "ApplicationProperties{" +
                "name='" + name + '\'' +
                ", proxy='" + proxy + '\'' +
                '}';
    }
}
