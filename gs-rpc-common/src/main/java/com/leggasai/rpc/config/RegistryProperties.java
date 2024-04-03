package com.leggasai.rpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-12:49
 * @Description: 注册中心配置
 */
@Configuration
public class RegistryProperties {
    @Value("${gsrpc.registry.host:127.0.0.1}")
    private String host;
    @Value("${gsrpc.registry.port:2181}")
    private Integer port;
    @Value("${gsrpc.registry.type:zookeeper}")
    private String type;
    @Value("${gsrpc.registry.timeout:5000}")
    private Integer timeout;
    @Value("${gsrpc.registry.session:60000}")
    private Integer session;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getSession() {
        return session;
    }

    public void setSession(Integer session) {
        this.session = session;
    }

    /**
     * just for test, fixme
     * @return
     */

    @Override
    public String toString() {
        return "RegistryProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", type='" + type + '\'' +
                ", timeout=" + timeout +
                ", session=" + session +
                '}';
    }
}
