package com.leggasai.rpc.client.proxy.javassist;

import com.leggasai.rpc.client.proxy.invoke.MethodInvoke;
import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

public class JavassistInvokeAdapter implements MethodHandler {
    private final MethodInvoke invoker;

    public JavassistInvokeAdapter(MethodInvoke methodInvoke) {
        this.invoker = methodInvoke;
    }
    @Override
    public Object invoke(Object o, Method method, Method method1, Object[] objects) throws Throwable {
        return invoker.invoke(o, method, objects);
    }
}
