package com.leggasai.rpc.client.proxy;

public interface IProxyFactory {
    <T> T getProxy(Class<T> clazz,String service,String version);
}
