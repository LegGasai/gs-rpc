package com.leggasai.rpc.client.proxy.jdk;

import com.leggasai.rpc.client.proxy.invoke.MethodInvoke;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JdkInvokeAdapter implements InvocationHandler {

    private final MethodInvoke invoker;

    public JdkInvokeAdapter(MethodInvoke methodInvoke) {
        this.invoker = methodInvoke;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(proxy, method, args);
    }
}
