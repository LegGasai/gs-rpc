package com.leggasai.rpc.client.proxy.invoke;

import java.lang.reflect.Method;
/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-06-12:14
 * @Description: RPC方法执行,封装客户端执行rpc调用的统一逻辑
 */
public class RpcMethodInvoke implements MethodInvoke{

    private final String service;
    private final String version;

    public RpcMethodInvoke(String service, String version) {
        this.service = service;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        // todo
        // 提交请求到teskManager，返回future
        // return future.get()
        // 超时机制
        System.out.println(service + "#" + version + "#" + method.getName() + "#" );
        return null;
    }


}
