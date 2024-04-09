package com.leggasai.rpc.client.proxy.invoke;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.config.ConsumerProperties;

public class RpcMethodInvokeFactory {

    public static MethodInvoke createMethodInvoke(String service, String version,ConsumerProperties consumerProperties, InvocationManager invocationManager) {
        RpcMethodInvoke rpcMethodInvoke = new RpcMethodInvoke(service, version);
        rpcMethodInvoke.setInvocationManager(invocationManager);
        rpcMethodInvoke.setConsumerProperties(consumerProperties);
        return rpcMethodInvoke;
    }
}
