package com.leggasai.rpc.client.proxy.javassist;

import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;

public class JavassistProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version) {
        try {
            return (T)JavassistProxyGenerator.newProxyInstance(clazz.getClassLoader(), clazz, new RpcMethodInvoke(service,version));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
