package com.leggasai.rpc.gsrpcspringboot.provider.impl;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-17:02
 * @Description:
 */
@GsService()
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public Integer add(Integer a, Integer b) {
        return a + b;
    }


}