package com.leggasai.rpc.client.proxy;

import com.leggasai.rpc.client.proxy.cglib.CglibProxyFactory;
import com.leggasai.rpc.client.proxy.javassist.JavassistProxyFactory;
import com.leggasai.rpc.client.proxy.jdk.JdkProxyFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProxyFactoryBuilder {
    private static final Map<String,IProxyFactory> FACTORIES;
    static {
        FACTORIES = new HashMap<>();
        FACTORIES.put("javassist", new JavassistProxyFactory());
        FACTORIES.put("jdk", new JdkProxyFactory());
        FACTORIES.put("cglib", new CglibProxyFactory());
    }

    public static IProxyFactory getProxyFactory(ProxyType proxyType) {
        IProxyFactory factory = FACTORIES.get(proxyType.getProxyType());
        if (factory == null) {
            throw new IllegalArgumentException("No such proxy type: " + proxyType);
        }
        return factory;
    }
}
