package com.leggasai.rpc.client.proxy.invoke;

import java.lang.reflect.Method;

public interface MethodInvoke {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
