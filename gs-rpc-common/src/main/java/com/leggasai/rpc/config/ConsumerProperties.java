package com.leggasai.rpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-19:09
 * @Description: 服务调用者属性配置
 */
@Configuration
public class ConsumerProperties {
    @Value("${gsrpc.consumer.timeout:10000}")
    private Integer timeout;

    @Value("${gsrpc.consumer.retries:0}")
    private Integer retries;

    @Value("${gsrpc.consumer.loadbalance:RoundRobin}")
    private String loadBalance;

    @Value("${gsrpc.consumer.protocol:kindred}")
    private String protocol;

    @Value("${gsrpc.consumer.port:-1}")
    private Integer port;

    @Value("${gsrpc.consumer.serialization:kryo}")
    private String serialization;


    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    /**
     * just for test, fixme
     * @return
     */
    @Override
    public String toString() {
        return "ConsumerProperties{" +
                "timeout=" + timeout +
                ", retries=" + retries +
                ", loadBalance='" + loadBalance + '\'' +
                ", protocol='" + protocol + '\'' +
                ", port=" + port +
                ", serialization='" + serialization + '\'' +
                '}';
    }
}
