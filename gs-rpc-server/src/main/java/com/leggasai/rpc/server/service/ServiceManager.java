package com.leggasai.rpc.server.service;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.common.beans.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
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

    /**
     * ServiceKey -> ServiceObject
     */
    private Map<String,Object> servicesMap = new HashMap<String,Object>();
    private Map<String,FastClass> services2FastClass = new HashMap<String,FastClass>();
    private Set<ServiceMeta> serviceMetaSet = new HashSet<ServiceMeta>();

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    public void cacheService(ServiceMeta serviceMeta,Object implObj){
        servicesMap.put(serviceMeta.getServiceKey(),implObj);
        Class<?> serviceClazz = implObj.getClass();
        FastClass serviceFastClass = FastClass.create(serviceClazz);
        services2FastClass.put(serviceMeta.getServiceKey(),serviceFastClass);
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
        logger.info("ServiceManager starts up successfully and caches all serviceImpls");
    }
    public boolean isAnyService(){
        return !serviceMetaSet.isEmpty();
    }

    public Object getService(String serviceKey){
        return servicesMap.get(serviceKey);
    }

    public FastClass getServiceFastClass(String serviceKey){
        return services2FastClass.get(serviceKey);
    }

}
