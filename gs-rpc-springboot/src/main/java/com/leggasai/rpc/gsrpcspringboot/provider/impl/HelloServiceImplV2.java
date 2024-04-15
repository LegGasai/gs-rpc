package com.leggasai.rpc.gsrpcspringboot.provider.impl;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-17:14
 * @Description:
 */
@GsService(version = "2.0")
public class HelloServiceImplV2 implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name + "#version2.0";
    }

    @Override
    public Integer add(Integer a, Integer b) {
        return (a+b) * 2;
    }

    @Override
    public void show() {

    }
}
