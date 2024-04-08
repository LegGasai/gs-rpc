package com.leggasai.rpc.client.proxy.cglib;

import com.leggasai.rpc.client.proxy.AbstractIProxyFactory;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import com.leggasai.rpc.client.proxy.jdk.JdkInvokeAdapter;
import org.springframework.cglib.proxy.Enhancer;

import java.lang.reflect.Proxy;

public class CglibProxyFactory extends AbstractIProxyFactory {
    @Override
    protected <T> T createProxy(Class<T> clazz, String service, String version) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibInvokeAdapter(new RpcMethodInvoke(service,version)));
        return (T) enhancer.create();
    }
}
