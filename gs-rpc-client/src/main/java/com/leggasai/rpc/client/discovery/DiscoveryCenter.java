package com.leggasai.rpc.client.discovery;

import com.google.common.collect.Collections2;
import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.netty.ConnectionPoolManager;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.RegistryProperties;
import com.leggasai.rpc.utils.PathUtil;
import com.leggasai.rpc.zookeeper.CuratorClient;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-22:03
 * @Description: 服务发现订阅
 */
@Component
public class DiscoveryCenter{
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryCenter.class);
    private static final String SPLIT_TOKEN = "/";
    private static final String SERVICE_PREFIX = "services";

    @Autowired
    private RegistryProperties registryProperties;

    @Autowired
    private ConnectionPoolManager connectionPoolManager;

    private CuratorClient curatorClient;


    /**
     * 监视器Map
     */
    private ConcurrentHashMap<String, CuratorCache> curatorCacheMap = new ConcurrentHashMap<>();

    /**
     * 缓存客户端所需所有服务信息
     */
    private Set<ServiceMeta> serviceCache = new HashSet<ServiceMeta>();

    /**
     * ServiceKey -> Invokers映射
     */
    private ConcurrentHashMap<String, CopyOnWriteArraySet<Invoker>> service2Invokers = new ConcurrentHashMap<>();

    /**
     * ProviderKey -> Invoker映射
     */
    private ConcurrentHashMap<String, Invoker> invokersMap = new ConcurrentHashMap<>();

    /**
     * ProviderKey -> Services映射
     */
    private ConcurrentHashMap<String, CopyOnWriteArraySet<String>> invoker2Services = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        String host = registryProperties.getHost();
        Integer port = registryProperties.getPort();
        Integer timeout = registryProperties.getTimeout();
        Integer session = registryProperties.getSession();
        this.curatorClient = new CuratorClient(host,port,timeout,session);
        if (this.curatorClient.isConnected()){
            this.curatorClient.watchState(new DiscoveryConnectionStateListener(this,timeout,session));
            logger.info("DiscoveryCenter has been connected to remote registry center {}:{}",host,port);
        }
    }

    /**
     * 从注册中心订阅服务，并添加监听
     */
    public void subscribeService(){
        if (!curatorClient.isConnected()){
            logger.error("Registry services fails, cannot connect to the registry center {}:{}",registryProperties.getHost(),registryProperties.getPort());
            return;
        }

        for (ServiceMeta serviceMeta : serviceCache) {
            logger.info("DiscoveryCenter is subscribing service {}",serviceMeta.getServiceKey());
            String serviceKey = serviceMeta.getServiceKey();
            String path = PathUtil.buildPath(SPLIT_TOKEN, SERVICE_PREFIX, serviceKey);
            // add path listener
            if (curatorClient.checkExits(path)){
                CuratorCache pathWatch = curatorClient.watchPath(path, new DiscoveryServiceListener(this, serviceKey, path));
                curatorCacheMap.put(serviceKey, pathWatch);
            }

            List<String> providerKeys = curatorClient.getChildren(path);
            if (providerKeys == null || providerKeys.isEmpty()){
                logger.warn("There are no valid provider for this service {}",serviceMeta.getServiceKey());
                continue;
            }
            CopyOnWriteArraySet<Invoker> invokerSet = service2Invokers.computeIfAbsent(serviceKey, k -> new CopyOnWriteArraySet<>());
            for (String providerKey : providerKeys) {
                String providerPath = path + SPLIT_TOKEN + providerKey;
                String providerUrl = curatorClient.getData(providerPath);
                Invoker invoker = invokersMap.getOrDefault(providerKey,new Invoker(providerUrl));
                invokerSet.add(invoker);
                invokersMap.put(providerKey, invoker);
                addServiceToInvoker(providerKey,serviceKey,invoker);
                logger.info("DiscoveryCenter has subscribed service {} from provider {}",serviceMeta.getServiceKey(),providerKey);
            }
        }
    }

    public void reSubscribe(){
        if (!curatorCacheMap.values().isEmpty()) {
            closeListener();
        }
        logger.info("DiscoveryCenter is resubscribing services");
        subscribeService();
        logger.info("DiscoveryCenter has resubscribed services successfully");
    }

    public void addInvoker(String serviceKey, String providerKey, String providerUrl){
        Invoker invoker = invokersMap.getOrDefault(providerKey,new Invoker(providerUrl));
        addServiceToInvoker(providerKey,serviceKey,invoker);
        CopyOnWriteArraySet<Invoker> invokers = service2Invokers.computeIfAbsent(serviceKey, k -> new CopyOnWriteArraySet<>());
        invokers.add(invoker);
        invokersMap.put(providerKey, invoker);
        logger.info("DiscoveryCenter has added new provider {} for service {}",providerKey,serviceKey);
    }

    public void updateInvoker(String serviceKey, String providerKey, String providerUrl) {
        Invoker invoker = invokersMap.get(providerKey);
        if(invoker != null){
            invoker.updateUrl(providerUrl);
            logger.info("DiscoveryCenter has update the provider {} for service {}",invoker.toString(),serviceKey);
        }
    }

    public void removeInvoker(String serviceKey, String providerKey){
        Invoker invoker = invokersMap.get(providerKey);
        if (invoker != null){
            CopyOnWriteArraySet<Invoker> invokers = service2Invokers.get(serviceKey);
            removeServiceFromInvoker(providerKey,serviceKey,invoker);
            if (invokers != null){
                invokers.remove(invoker);
            }
            logger.info("DiscoveryCenter has removed provider {} for service {}, because it has been disabled.",providerKey,serviceKey);
        }
    }

    public void saveService(ServiceMeta serviceMeta){
        serviceCache.add(serviceMeta);
        logger.info("Discover service {} and wait for subscription",serviceMeta.getServiceKey());
    }

    private void addServiceToInvoker(String providerKey, String serviceKey, Invoker invoker){
        CopyOnWriteArraySet<String> services = invoker2Services.computeIfAbsent(providerKey, k -> new CopyOnWriteArraySet<>());
        services.add(serviceKey);
        connectionPoolManager.getHandler(invoker);
        logger.info("DiscoveryCenter has added service {} to provider {}",serviceKey,providerKey);
    }

    private void removeServiceFromInvoker(String providerKey, String serviceKey, Invoker invoker){
        CopyOnWriteArraySet<String> services = invoker2Services.computeIfAbsent(providerKey, k -> new CopyOnWriteArraySet<>());
        services.remove(serviceKey);
        if (services.size() == 0){
            connectionPoolManager.removeAndCloseHandler(invoker);
            invoker2Services.remove(providerKey);
            invokersMap.remove(providerKey);
            logger.info("DiscoveryCenter has remove provider{} because it now does not provide any services",providerKey);
        }
    }

    /**
     * just for debug, fixme
     * @return
     */
    public Set<ServiceMeta> getService(){
        System.out.println(serviceCache);
        System.out.println(service2Invokers);
        System.out.println(invokersMap);
        return serviceCache;
    }

    public List<Invoker> getInvokersByServiceKey(String serviceKey){
        CopyOnWriteArraySet<Invoker> invokers = service2Invokers.get(serviceKey);
        if (CollectionUtils.isEmpty(invokers)){
            return new ArrayList<>();
        }
        return new ArrayList<>(service2Invokers.get(serviceKey));
    }

    private void closeListener(){
        for (CuratorCache cache : curatorCacheMap.values()) {
            cache.close();
        }
        logger.info("DiscoveryCenter has closed all listeners");
    }


    public void close(){
        closeListener();
        curatorClient.close();
        logger.info("DiscoveryCenter has been closed and disconnected from remote registry center {}:{}",registryProperties.getHost(),registryProperties.getPort());
    }

}
