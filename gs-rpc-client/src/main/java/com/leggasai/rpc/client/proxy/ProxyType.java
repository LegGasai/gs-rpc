package com.leggasai.rpc.client.proxy;

import com.leggasai.rpc.serialization.SerializationType;

public enum ProxyType {
    JDK("jdk"),
    CGLIB("cglib"),
    JAVASSIST("javassist");


    private final String type;

    ProxyType(String type) {
        this.type = type;
    }

    public String getProxyType() {
        return type;
    }

    public static ProxyType getByProxy(String proxyType){
        switch (proxyType){
            case "jdk":
                return JDK;
            case "cglib":
                return CGLIB;
            case "javassist":
                return JAVASSIST;
            default:
                throw new RuntimeException("不支持的动态代理类型");
        }
    }
}
