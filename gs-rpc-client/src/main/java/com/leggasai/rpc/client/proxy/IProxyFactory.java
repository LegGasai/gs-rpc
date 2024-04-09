package com.leggasai.rpc.client.proxy;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.config.ConsumerProperties;

public interface IProxyFactory {
    <T> T getProxy(Class<T> clazz, String service, String version, ConsumerProperties consumerProperties, InvocationManager invocationManager);
}
