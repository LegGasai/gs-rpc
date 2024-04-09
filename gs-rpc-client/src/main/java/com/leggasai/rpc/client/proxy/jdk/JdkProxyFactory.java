package com.leggasai.rpc.client.proxy.jdk;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvokeFactory;
import com.leggasai.rpc.config.ConsumerProperties;

import java.lang.reflect.Proxy;

public class JdkProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version, ConsumerProperties consumerProperties, InvocationManager invocationManager) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new JdkInvokeAdapter(RpcMethodInvokeFactory.createMethodInvoke(service,version,consumerProperties,invocationManager)));
    }
}
