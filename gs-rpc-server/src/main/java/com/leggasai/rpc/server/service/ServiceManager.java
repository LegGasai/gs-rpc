package com.leggasai.rpc.server.service;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.zookeeper.CuratorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-16:24
 * @Description: 服务管理器
 */
@Component
public class ServiceManager implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private Map<String,Object> servicesMap = new HashMap<String,Object>();

    private Set<ServiceMeta> serviceMetaSet = new HashSet<ServiceMeta>();

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public void cacheService(ServiceMeta serviceMeta,Object implObj){
        servicesMap.put(serviceMeta.getServiceKey(),implObj);
        serviceMetaSet.add(serviceMeta);
    }

    public Set<ServiceMeta> getAllServices(){
        return serviceMetaSet;
    }

    @PostConstruct
    public void scanService(){
        Map<String, Object> servicesMap = context.getBeansWithAnnotation(GsService.class);
        for (Object implObj : servicesMap.values()) {
            GsService annotation = implObj.getClass().getAnnotation(GsService.class);
            String version = annotation.version();
            Class<?>[] interfaces = implObj.getClass().getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                String serviceName = interfaceClass.getName();
                ServiceMeta serviceMeta = new ServiceMeta(serviceName, version);
                cacheService(serviceMeta,implObj);
            }
        }
    }
}
