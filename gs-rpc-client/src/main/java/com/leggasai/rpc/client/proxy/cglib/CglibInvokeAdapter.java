package com.leggasai.rpc.client.proxy.cglib;

import com.leggasai.rpc.client.proxy.invoke.RpcMethodInvoke;
import io.protostuff.Rpc;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CglibInvokeAdapter implements MethodInterceptor {

    private final RpcMethodInvoke invoker;

    public CglibInvokeAdapter(RpcMethodInvoke rpcMethodInvoke) {
        this.invoker = rpcMethodInvoke;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return invoker.invoke(o, method, objects);
    }
}
