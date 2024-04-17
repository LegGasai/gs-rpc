package com.leggasai.rpc.gsrpcspringboot.consumer.service;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;
import com.leggasai.rpc.gsrpcspringboot.api.dto.Order;
import com.sun.org.apache.xpath.internal.operations.Or;
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

    public void demoTest(){
        System.out.println(helloService.hello("我是朱瑞祥"));
        System.out.println(helloService2.hello("我是sb"));
    }

    public String helloTest(String name){
        return helloService.hello(name);
    }

    public String helloTest2(String name){
        return helloService2.hello(name);
    }

    public void show(){
        helloService.show();
    }


    public Order getOrder(Order order){
        return  helloService.getOrder(order);
    }

    public void timeout(){
        helloService.timeout();
    }


}
