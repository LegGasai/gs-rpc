package com.leggasai.rpc.test.client.route.loadBalance;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceFactory;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceType;
import com.leggasai.rpc.client.route.loadBalance.impl.RoundRobinLoadBalance;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class LoadBalanceTest {

    // fixme
    @Test
    public void roundRobinTest() {
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.ROUND_ROBIN);
        Invoker invoker11 = new Invoker("127.0.0.1:8081?weight=2");
        Invoker invoker12 = new Invoker("127.0.0.1:8082?weight=3");
        Invoker invoker13 = new Invoker("127.0.0.1:8083?weight=5");
        List<Invoker> invokers = new ArrayList<>();
        invokers.add(invoker11);
        invokers.add(invoker12);
        invokers.add(invoker13);

        Invocation invocation = new Invocation();
        RpcRequestBody request1 = new RpcRequestBody();
        request1.setMethod("method");
        request1.setService("service");
        request1.setParameters(new Object[]{"1"});
        invocation.setRequest(request1);
        HashMap<Invoker, Integer> invokerMap = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);

        // 移除最后一个节点
        invokers.remove(2);
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);
        // 新增一个节点 2 3 4
        Invoker invoker14 = new Invoker("127.0.0.1:8084?weight=4");
        invokers.add(invoker14);
        invokerMap.clear();
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);
        //修改节点权重
        invoker11.updateUrl("127.0.0.1:8081?weight=5");
        invoker12.updateUrl("127.0.0.1:8082?weight=3");
        invoker14.updateUrl("127.0.0.1:8083?weight=2");
        invokerMap.clear();
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);

        // Invocation互不影响

    }

    @Test
    public void roundRobinMultiInvocationTest(){

        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.ROUND_ROBIN);
        Invoker invoker11 = new Invoker("127.0.0.1:8081?weight=2");
        Invoker invoker12 = new Invoker("127.0.0.1:8082?weight=3");
        Invoker invoker21 = new Invoker("127.0.0.1:8083?weight=5");
        Invoker invoker22 = new Invoker("127.0.0.1:8084?weight=1");
        List<Invoker> invokers1 = new ArrayList<>();
        List<Invoker> invokers2 = new ArrayList<>();

        invokers1.add(invoker11);
        invokers1.add(invoker12);
        invokers1.add(invoker21);

        invokers2.add(invoker12);
        invokers2.add(invoker21);
        invokers2.add(invoker22);

        Invocation invocation = new Invocation();
        RpcRequestBody request1 = new RpcRequestBody();
        request1.setMethod("method");
        request1.setService("service");
        request1.setParameters(new Object[]{"1"});
        invocation.setRequest(request1);


        HashMap<Invoker, Integer> invokerMap = new HashMap<>();
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers1,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);

        invokerMap.clear();
        request1.setService("service1");
        for (int i = 0; i < 9999; i++) {
            Invoker invoker = loadBalance.select(invokers2,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);

        invokerMap.clear();
        request1.setMethod("method1");
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers2,invocation);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        System.out.println(invokerMap);

    }

    @Test
    public void consistentHashTest() {
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.CONSISTENT_HASH);
        Invoker invoker11 = new Invoker("127.0.0.1:8081?weight=2");
        Invoker invoker12 = new Invoker("127.0.0.1:8082?weight=3");
        Invoker invoker13 = new Invoker("127.0.0.1:8083?weight=5");
        List<Invoker> invokers = Arrays.asList(invoker11, invoker12, invoker13);
        Invocation invocation = new Invocation();

        // 构建RpcRequestBody对象request1，无参请求。应该随机调用
        RpcRequestBody request1 = new RpcRequestBody();
        invocation.setRequest(request1);
        request1.setMethod("method1");
        for (int i = 0; i < 10; i++) {
            Invoker select = loadBalance.select(invokers, invocation);
            System.out.println(select);
        }
        System.out.println("========================================");
        // 构建构建RpcRequestBody对象request2，有参数请求。
        // 3
        RpcRequestBody request2 = new RpcRequestBody();
        request2.setService("service");
        request2.setMethod("method");
        request2.setParameters(new Object[]{1, 2, 3});
        invocation.setRequest(request2);
        for (int i = 0; i < 10; i++) {
            Invoker select = loadBalance.select(invokers, invocation);
            System.out.println(select);
        }
        System.out.println("========================================");
        // 5
        request2.setParameters(new Object[]{17});
        for (int i = 0; i < 10; i++) {
            Invoker select = loadBalance.select(invokers, invocation);
            System.out.println(select);
        }

    }

    @Test
    public void consistentHashNotChangeTest() {
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.CONSISTENT_HASH);
        Invoker invoker11 = new Invoker("127.0.0.1:8081?weight=2");
        Invoker invoker12 = new Invoker("127.0.0.1:8082?weight=3");
        Invoker invoker13 = new Invoker("127.0.0.1:8083?weight=5");
        ArrayList<Invoker> invokers1 = new ArrayList<>();
        ArrayList<Invoker> invokers2 = new ArrayList<>();
        invokers1.add(invoker11);
        invokers1.add(invoker12);
        invokers1.add(invoker13);
        invokers2.add(invoker11);
        invokers2.add(invoker12);

        Invocation invocation = new Invocation();
        // Case1
        // 构建RpcRequestBody对象request1，无参请求。应该随机调用
        RpcRequestBody request1 = new RpcRequestBody();
        request1.setMethod("method");
        request1.setService("service");
        request1.setParameters(new Object[]{"1"});
        invocation.setRequest(request1);
        // Case2
        // if a==n-1 then a == b
        // 5
        System.out.println(loadBalance.select(invokers1,invocation));
        System.out.println("========================================");
        // 3
        System.out.println(loadBalance.select(invokers2,invocation));

        // Case3
        // if a==n-1 then a != b
        request1.setParameters(new Object[]{1,2,3});
        //3
        System.out.println(loadBalance.select(invokers1,invocation));
        System.out.println("========================================");
        //3
        System.out.println(loadBalance.select(invokers2,invocation));

        //Case4 no invokers
        System.out.println(loadBalance.select(new ArrayList<>(),invocation));
        System.out.println("========================================");
        //Case new invokers
        Invoker invoker14 = new Invoker("127.0.0.1:8084?weight=10");
        ArrayList<Invoker> invokers3 = new ArrayList<>();
        invokers3.add(invoker11);
        invokers3.add(invoker12);
        invokers3.add(invoker13);
        invokers3.add(invoker14);
        // 3 keep
        System.out.println(loadBalance.select(invokers3,invocation));
        request1.setParameters(new Object[]{"1"});
        // 5
        System.out.println(loadBalance.select(invokers3,invocation));
        request1.setParameters(new Object[]{12});
        // 10
        System.out.println(loadBalance.select(invokers3,invocation));
    }


    @Test
    public void randomLoadBalanceTest(){
        Invoker invoker11 = new Invoker("127.0.0.1:8081?weight=2");
        Invoker invoker12 = new Invoker("127.0.0.1:8082?weight=3");
        Invoker invoker13 = new Invoker("127.0.0.1:8083?weight=5");
        HashMap<Invoker, Integer> invokerMap = new HashMap<>();

        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.RANDOM);
        List<Invoker> invokers = new ArrayList<>();
        invokers.add(invoker11);
        invokers.add(invoker12);
        invokers.add(invoker13);
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers, null);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0)+1);
        }
        // it should be near to 2:3:5
        System.out.println(invokerMap);
        System.out.println("=============================================");
        invokerMap.clear();
        Invoker invoker14 = new Invoker("127.0.0.1:8083?weight=0");
        invokers.add(invoker14);
        for (int i = 0; i < 10000; i++) {
            Invoker invoker = loadBalance.select(invokers, null);
            invokerMap.put(invoker,invokerMap.getOrDefault(invoker,0) + 1);
        }
        System.out.println(invokerMap);
    }


}
