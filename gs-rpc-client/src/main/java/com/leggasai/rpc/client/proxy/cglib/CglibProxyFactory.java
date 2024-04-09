package com.leggasai.rpc.client.proxy.cglib;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvokeFactory;
import com.leggasai.rpc.client.proxy.jdk.JdkInvokeAdapter;
import com.leggasai.rpc.config.ConsumerProperties;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;

public class CglibProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version, ConsumerProperties consumerProperties, InvocationManager invocationManager) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibInvokeAdapter(RpcMethodInvokeFactory.createMethodInvoke(service,version,consumerProperties,invocationManager)));
        return (T) enhancer.create();
    }
}
