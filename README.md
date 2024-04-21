# GS-RPC
* A java-based RPC framework

## Features
* Easy to use
* Service registration and discovery based on Zookeeper
* Multi-policy load balancing
* Multiple high-performance serialization protocols, such as Kryo, fst, hessian2, protostuff

## Quick Start
1. Define service interfaces

Interfaces need to be defined on both the consumer and provider side.
```java
package com.leggasai.rpc.gsrpcspringboot.api;

public interface HelloService {
    String hello(String name);
    Integer add(Integer a,Integer b);
}
```

2. Implement service interface on the provider

Implement the interface behavior at the service provider and mark it up with annotations `GsService()`
```java
package com.leggasai.rpc.gsrpcspringboot.provider.impl;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;

import java.util.Date;

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
```

Also we can use different `version` field to define another Implementation.
```java
package com.leggasai.rpc.gsrpcspringboot.provider.impl;

import com.leggasai.rpc.annotation.GsService;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;

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

}
```

3. Referencing services on the consumer.

Referencing a specific `version` or `null` of a service via annotations `GsReference`
```java
package com.leggasai.rpc.gsrpcspringboot.consumer.service;

import com.leggasai.rpc.annotation.GsReference;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;
import org.springframework.stereotype.Component;

@Component
public class DemoService {

    @GsReference()
    private HelloService helloService;

    @GsReference(version = "2.0")
    private HelloService helloService2;
    

    public String helloTest(String name){
        return helloService.hello(name);
    }

    public String helloTest2(String name){
        return helloService2.hello(name);
    }
}

```
4. Configure `Application.yml` or `Application.properties` file

More configuration can be found here
```yaml
gsrpc:
  application:
    name: rpc-springboot-demo
    proxy: cglib

  provider:
    timeout: 1000
    retries: 1
    protocol: kindred
    serialization: kryo
    weight: 5
    host: 127.0.0.1
    port: 20888
    accepts: 1024

  consumer:
    timeout: 5000
    retries: 1
    loadbalance: random
    protocol: kindred
    port: -1
    serialization: kryo

  registry:
    host: 127.0.0.1  # your zookeeper host
    port: 2181       # your zookeeper port
    session: 5000


```

5. Start your application

Note the `EnableGsRPC()` annotation on your SpringBoot Application class  
```java
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
        
        DemoService demoService = context.getBean(DemoService.class);
        String message1 = demoService.helloTest("I am gsrpc");
        System.out.println(message1);
        
        String message2 = demoService.helloTest2("I am gsrpc");
        System.out.println(message2);
    }

}

```