package com.leggasai.rpc.gsrpcspringboot.api;


import com.leggasai.rpc.gsrpcspringboot.api.dto.Order;
import com.sun.org.apache.xpath.internal.operations.Or;

public interface HelloService {
    String hello(String name);

    Integer add(Integer a,Integer b);

    void show();


    Order getOrder(Order order);

    void timeout();

}
