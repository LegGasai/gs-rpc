package com.leggasai.rpc.gsrpcspringboot.consumer.service;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;
import org.springframework.stereotype.Component;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-22:15
 * @Description:
 */
@Component
public class DemoService {

    @GsReference()
    private HelloService helloService;

    @GsReference(version = "2.0")
    private HelloService helloService2;
}
