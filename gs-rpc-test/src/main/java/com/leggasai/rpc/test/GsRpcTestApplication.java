package com.leggasai.rpc.test;

import com.leggasai.rpc.annotation.EnableGsRPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-14-19:59
 * @Description:
 */
@SpringBootApplication
@EnableGsRPC
public class GsRpcTestApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(GsRpcTestApplication.class, args);
    }
}
