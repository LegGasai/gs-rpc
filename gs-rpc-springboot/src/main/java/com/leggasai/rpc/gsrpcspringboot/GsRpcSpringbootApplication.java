package com.leggasai.rpc.gsrpcspringboot;

import com.leggasai.rpc.config.ProviderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GsRpcSpringbootApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GsRpcSpringbootApplication.class, args);

        ProviderProperties bean = context.getBean(ProviderProperties.class);
        System.out.println(bean.getName());
    }

    @Bean
    public ProviderProperties providerProperties() {
        return new ProviderProperties();
    }
}
