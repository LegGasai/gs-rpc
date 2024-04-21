package com.leggasai.rpc.gsrpcspringboot;

import com.leggasai.rpc.annotation.EnableGsRPC;
import com.leggasai.rpc.config.ApplicationProperties;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.gsrpcspringboot.consumer.service.DemoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
@EnableGsRPC
public class GsRpcSpringbootApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GsRpcSpringbootApplication.class, args);

        ProviderProperties providerProperties = context.getBean(ProviderProperties.class);
        ApplicationProperties applicationProperties = context.getBean(ApplicationProperties.class);
        ConsumerProperties consumerProperties = context.getBean(ConsumerProperties.class);

        System.out.println(applicationProperties);
        System.out.println(providerProperties);
        System.out.println(consumerProperties);


    }

}
