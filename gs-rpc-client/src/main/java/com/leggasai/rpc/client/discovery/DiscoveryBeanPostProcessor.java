package com.leggasai.rpc.client.discovery;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.proxy.IProxyFactory;
import com.leggasai.rpc.client.proxy.ProxyFactoryBuilder;
import com.leggasai.rpc.client.proxy.ProxyType;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.ApplicationProperties;
import com.leggasai.rpc.config.ConsumerProperties;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-22:10
 * @Description:
 */
@Component
public class DiscoveryBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveryBeanPostProcessor.class);
    @Autowired
    private DiscoveryCenter discoveryCenter;
    @Autowired
    private ApplicationProperties applicationProperties;
    @Autowired
    private ConsumerProperties consumerProperties;
    @Autowired
    private InvocationManager invocationManager;
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(GsReference.class)){
                GsReference gsReference = field.getAnnotation(GsReference.class);
                String version = gsReference.version();
                String serviceName = field.getType().getName();
                ServiceMeta serviceMeta = new ServiceMeta(serviceName, version);
                discoveryCenter.saveService(serviceMeta);
                // 创建代理
                try {
                    IProxyFactory proxyFactory = ProxyFactoryBuilder.getProxyFactory(ProxyType.getByProxy(applicationProperties.getProxy()));
                    Object proxy = proxyFactory.getProxy(field.getType(), serviceName, version,consumerProperties, invocationManager);
                    field.set(bean, proxy);
                    logger.info("创建Service代理成功，serviceKey:{}#{},proxy:{}",serviceName,version,proxy);
                } catch (IllegalAccessException e) {
                    logger.error("创建Service代理失败，serviceKey:{}#{}",serviceName,version,e);
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }

}
