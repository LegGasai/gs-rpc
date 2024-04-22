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
    @Value("${gsrpc.provider.timeout:5000}")
    private Integer timeout;
    @Value("${gsrpc.provider.retries:0}")
    private Integer retries;
    @Value("${gsrpc.provider.protocol:kindred}")
    private String protocol;
    @Value("${gsrpc.provider.host:#{null}}")
    private String host;
    @Value("${gsrpc.provider.port:20688}")
    private Integer port;
    @Value("${gsrpc.provider.serialization:kryo}")
    private String serialization;
    @Value("${gsrpc.provider.weight:5}")
    private Integer weight;
    @Value("${gsrpc.provider.accepts:1024}")
    private Integer accepts;

    @Value("${gsrpc.provider.registry.host:127.0.0.1}")
    private String registryHost;
    @Value("${gsrpc.provider.registry.port:2181}")
    private Integer registryPort;
    @Value("${gsrpc.provider.registry.type:zookeeper}")
    private String registryType;
    @Value("${gsrpc.provider.registry.timeout:5000}")
    private Integer registryTimeout;
    @Value("${gsrpc.provider.registry.session:60000}")
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

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getAccepts() {
        return accepts;
    }

    public void setAccepts(Integer accepts) {
        this.accepts = accepts;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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
        return "ProviderProperties{" +
                "timeout=" + timeout +
                ", retries=" + retries +
                ", protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", serialization='" + serialization + '\'' +
                ", weight=" + weight +
                ", accepts=" + accepts +
                ", registryHost='" + registryHost + '\'' +
                ", registryPort=" + registryPort +
                ", registryType='" + registryType + '\'' +
                ", registryTimeout=" + registryTimeout +
                ", registrySession=" + registrySession +
                '}';
    }
}
