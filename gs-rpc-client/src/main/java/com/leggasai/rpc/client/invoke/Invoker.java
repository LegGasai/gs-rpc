package com.leggasai.rpc.client.invoke;

import com.leggasai.rpc.common.beans.RpcURL;

import java.util.Objects;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-10:19
 * @Description: 服务提供者信息
 */

// fixme Invoker的地址和端口号时不可变的，但可以修改其属性
public class Invoker {

    private static final String WEIGHT_KEY = "weight";
    private final String host;
    private final Integer port;

    private RpcURL url;
    public Invoker(String url) {
        RpcURL rpcURL = new RpcURL(url);
        this.url = rpcURL;
        this.host = rpcURL.getHost();
        this.port = rpcURL.getPort();
    }

    public void updateUrl(String url){
        this.url = new RpcURL(url);
    }

    public String getAddress(){
        return host + ":" + port;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public Integer getWeight() {
        return Integer.valueOf(url.getParameter(WEIGHT_KEY));
    }


    /**
     * just for test, fixme
     * @return
     */
    @Override
    public String toString() {
        return "Invoker{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", weight=" + getWeight() +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoker invoker = (Invoker) o;
        return Objects.equals(host, invoker.host) && Objects.equals(port, invoker.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
