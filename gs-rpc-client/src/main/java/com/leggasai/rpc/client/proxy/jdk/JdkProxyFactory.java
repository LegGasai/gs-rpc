package com.leggasai.rpc.client.proxy.jdk;

import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;

import java.lang.reflect.Proxy;

public class JdkProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new JdkInvokeAdapter(new RpcMethodInvoke(service,version)));
    }
}
