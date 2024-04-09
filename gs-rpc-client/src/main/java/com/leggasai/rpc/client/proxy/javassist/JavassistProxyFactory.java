package com.leggasai.rpc.client.proxy.javassist;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvokeFactory;
import com.leggasai.rpc.config.ConsumerProperties;

public class JavassistProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version, ConsumerProperties consumerProperties, InvocationManager invocationManager) {
        try {
            return (T)JavassistProxyGenerator.newProxyInstance(clazz.getClassLoader(), clazz, RpcMethodInvokeFactory.createMethodInvoke(service, version,consumerProperties, invocationManager));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
