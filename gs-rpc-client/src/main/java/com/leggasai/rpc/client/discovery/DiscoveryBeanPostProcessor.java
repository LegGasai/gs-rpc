package com.leggasai.rpc.client.discovery;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.common.beans.ServiceMeta;
import org.checkerframework.checker.units.qual.C;
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

    @Autowired
    private DiscoveryCenter discoveryCenter;
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
                // field.set(xxx,proxy)
            }
        }
        return bean;
    }

}
