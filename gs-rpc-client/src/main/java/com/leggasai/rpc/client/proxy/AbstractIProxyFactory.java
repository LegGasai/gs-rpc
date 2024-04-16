package com.leggasai.rpc.client.proxy;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.constants.Separator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractIProxyFactory implements IProxyFactory{
    protected static final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    protected abstract <T> T createProxy(Class<T> clazz,String service,String version,ConsumerProperties consumerProperties, InvocationManager invocationManager);

    @Override
    public <T> T getProxy(Class<T> clazz, String service, String version, ConsumerProperties consumerProperties, InvocationManager invocationManager) {
        String serviceKey = service + Separator.SERVICE_SPLIT+ version;
        Object proxy = proxyCache.get(serviceKey);
        if (proxy == null){
            proxy = createProxy(clazz,service,version,consumerProperties,invocationManager);
            proxyCache.put(serviceKey, proxy);
        }
        return (T)proxy;
    }
}
