package com.leggasai.rpc.gsrpcspringboot.consumer.service;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;
import org.springframework.stereotype.Component;

@Component
public class CacheService {
    @GsReference()
    private HelloService helloService;

    @GsReference(version = "2.0")
    private HelloService helloService2;
}
