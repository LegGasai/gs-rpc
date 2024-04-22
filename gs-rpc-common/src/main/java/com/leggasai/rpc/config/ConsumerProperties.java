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

    @Value("${gsrpc.consumer.registry.host:127.0.0.1}")
    private String registryHost;
    @Value("${gsrpc.consumer.registry.port:2181}")
    private Integer registryPort;
    @Value("${gsrpc.consumer.registry.type:zookeeper}")
    private String registryType;
    @Value("${gsrpc.consumer.registry.timeout:5000}")
    private Integer registryTimeout;
    @Value("${gsrpc.consumer.registry.session:60000}")
    private Integer registrySession;

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

    public String getRegistryHost() {
        return registryHost;
    }

    public Integer getRegistryPort() {
        return registryPort;
    }

    public String getRegistryType() {
        return registryType;
    }

    public Integer getRegistryTimeout() {
        return registryTimeout;
    }

    public Integer getRegistrySession() {
        return registrySession;
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
                ", registryHost='" + registryHost + '\'' +
                ", registryPort=" + registryPort +
                ", registryType='" + registryType + '\'' +
                ", registryTimeout=" + registryTimeout +
                ", registrySession=" + registrySession +
                '}';
    }
}
