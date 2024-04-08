package com.leggasai.rpc.client.proxy;

import com.leggasai.rpc.constants.Separator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractIProxyFactory implements IProxyFactory{
    protected final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    protected abstract <T> T createProxy(Class<T> clazz,String service,String version);

    @Override
    public <T> T getProxy(Class<T> clazz, String service, String version) {
        String serviceKey = service + Separator.SERVICE_SPLIT+ version;
        Object proxy = proxyCache.get(serviceKey);
        if (proxy == null){
            proxy = createProxy(clazz,service,version);
            proxyCache.put(serviceKey, proxy);
        }
        return (T)proxy;
    }
}
