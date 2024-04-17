package com.leggasai.rpc.gsrpcspringboot;

import com.leggasai.rpc.client.discovery.DiscoveryBeanPostProcessor;
import com.leggasai.rpc.client.discovery.DiscoveryCenter;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.netty.ConnectionPoolManager;
import com.leggasai.rpc.client.netty.handler.AbstractClientChannelHandler;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.common.beans.RpcURL;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.ApplicationProperties;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.config.RegistryProperties;
import com.leggasai.rpc.enums.ResponseType;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.gsrpcspringboot.api.HelloService;
import com.leggasai.rpc.gsrpcspringboot.api.dto.Order;
import com.leggasai.rpc.gsrpcspringboot.consumer.service.CacheService;
import com.leggasai.rpc.gsrpcspringboot.consumer.service.DemoService;
import com.leggasai.rpc.gsrpcspringboot.provider.impl.HelloServiceImpl;
import com.leggasai.rpc.gsrpcspringboot.provider.impl.HelloServiceImplV2;
import com.leggasai.rpc.protocol.heartbeat.HeartBeat;
import com.leggasai.rpc.protocol.kindred.Kindred;
import com.leggasai.rpc.serialization.RpcSerialization;
import com.leggasai.rpc.serialization.SerializationAdapter;
import com.leggasai.rpc.serialization.SerializationFactory;
import com.leggasai.rpc.serialization.SerializationType;
import com.leggasai.rpc.server.registry.RegistryCenter;
import com.leggasai.rpc.server.service.ServiceManager;
import com.leggasai.rpc.server.service.TaskManager;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import com.leggasai.rpc.threadpool.FixedThreadPool;
import com.leggasai.rpc.utils.NetUtil;
import com.leggasai.rpc.utils.Snowflake;
import com.sun.org.apache.bcel.internal.generic.NEW;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest
class GsRpcSpringbootApplicationTests {

    @Autowired
    private ApplicationContext context;
    @Test
    void contextLoads() {
        Long l = Snowflake.generateId();
        System.out.println(l);
    }

    /**
     * 各配置类测试
     */
    @Test
    public void propertiesTest(){
        ApplicationProperties applicationProperties = context.getBean(ApplicationProperties.class);
        ProviderProperties providerProperties = context.getBean(ProviderProperties.class);
        ConsumerProperties consumerProperties = context.getBean(ConsumerProperties.class);
        RegistryProperties registryProperties = context.getBean(RegistryProperties.class);
        System.out.println(applicationProperties);
        System.out.println(providerProperties);
        System.out.println(consumerProperties);
        System.out.println(registryProperties);
    }

    /**
     * 雪花算法测试
     */
    @Test
    public void snowflakeTest(){
        HashSet<Long> ids = new HashSet<>();
        for (int i = 0; i < 10000; i++) {
            Long id = Snowflake.generateId();
            Assert.isTrue(!ids.contains(id));
            ids.add(id);
        }
    }


    /**
     * 线程池测试
     */
    @Test
    public void threadpoolTest(){
        //ThreadPoolExecutor executor = (ThreadPoolExecutor)CachedThreadPool.getExecutor("demo", 0, 50, 5 * 1000);
        //for (int i = 0; i < 10; i++) {
        //    executor.execute(()->{
        //        System.out.println(Thread.currentThread().getName()+ ": Start in" + System.currentTimeMillis());
        //        try {
        //            Thread.sleep(5000);
        //            System.out.println(Thread.currentThread().getName()+ ": Finish in" + System.currentTimeMillis());
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //    });
        //}
        //executor.shutdown();
        //try {
        //    if (executor.awaitTermination(10, TimeUnit.SECONDS)){
        //        System.out.println("All tasks have been finished!");
        //    }
        //} catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}
        ThreadPoolExecutor executor = (ThreadPoolExecutor) FixedThreadPool.getExecutor("demo", 5,5);
        // This will cause rejectedExecutionException, since the size of queue is smaller than (10-5)
        // ThreadPoolExecutor executor = (ThreadPoolExecutor) FixedThreadPool.getExecutor("demo", 5,3);
        for (int i = 0; i < 10; i++) {
            executor.execute(()->{
                System.out.println(Thread.currentThread().getName()+ ": Start in" + System.currentTimeMillis());
                try {
                    Thread.sleep(5000);
                    System.out.println(Thread.currentThread().getName()+ ": Finish in" + System.currentTimeMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(10, TimeUnit.SECONDS)){
                System.out.println("All tasks have been finished!");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 注册中心测试
     */
    @Test
    public void registryTest(){
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        registryCenter.setRpcURL(new RpcURL("127.0.0.1:8899?weight=5"));
        registryCenter.unregister();
        registryCenter.register();
        // There should be some warnings that the nodes have been existed.
        registryCenter.reRegisterAllServices();
        registryCenter.close();
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册中心重连测试
     */
    @Test
    public void registryReconnectionTest(){
        RegistryCenter registryCenter = context.getBean(RegistryCenter.class);
        registryCenter.setRpcURL(new RpcURL("127.0.0.1:8899?weight=5"));
        registryCenter.unregister();
        registryCenter.register();
        try {
            // there will be reconnected to the register center
            Thread.sleep(60000);
            registryCenter.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 扫描服务测试
     */
    @Test
    public void scanServicesTest(){
        ServiceManager serviceManager = context.getBean(ServiceManager.class);
        Set<ServiceMeta> allServices = serviceManager.getAllServices();
        for (ServiceMeta service : allServices) {
            System.out.println(service.getServiceKey());
        }
    }


    @Test
    public void scanReferencesTest(){
        DiscoveryCenter discoveryCenter = context.getBean(DiscoveryCenter.class);
        System.out.println(discoveryCenter.getService());
    }


    @Test
    public void demoTest(){
        String serviceKeys[] = {"com.leggasai.rpc.gsrpcspringboot.api.HelloService#","com.leggasai.rpc.gsrpcspringboot.api.HelloService#2.0"};
        for (String serviceKey : serviceKeys) {
            String[] split = serviceKey.split("#");
            System.out.println(Arrays.toString(split));
        }
    }


    @Test
    public void subscribeServiceTest(){
        DiscoveryCenter discoveryCenter = context.getBean(DiscoveryCenter.class);
        // Test recurring subscriptions
        discoveryCenter.subscribeService();
        discoveryCenter.getService();
        try {
            // there will be reconnected to the register center
            Thread.sleep(600000);
            discoveryCenter.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void urlTest(){
        try {
            RpcURL rpcURL = new RpcURL("127.0.0.1:8899?version=5&timeout=6000");
            System.out.println(rpcURL);
            System.out.println(rpcURL.getHost());
            System.out.println(rpcURL.getPort());
            System.out.println(rpcURL.getParameters());
            RpcURL rpcURL1 = new RpcURL("127.0.0.1:8899");
            System.out.println(rpcURL1);
            System.out.println(rpcURL1.getHost());
            System.out.println(rpcURL1.getPort());
            System.out.println(rpcURL1.getParameters());
            RpcURL rpcURL2 = new RpcURL("127.0.0.1");
            System.out.println(rpcURL2);
            System.out.println(rpcURL2.getHost());
            System.out.println(rpcURL2.getPort());
            System.out.println(rpcURL2.getParameters());
            RpcURL rpcURL3 = new RpcURL("127.0.0.1:8899?version=5");
            System.out.println(rpcURL3);
            System.out.println(rpcURL3.getHost());
            System.out.println(rpcURL3.getPort());
            System.out.println(rpcURL3.getParameters());
            RpcURL rpcURL4 = new RpcURL("fe80::2654:8a7d:34d4:5b92?version=5");
            System.out.println(rpcURL4);
            System.out.println(rpcURL4.getHost());
            System.out.println(rpcURL4.getPort());
            System.out.println(rpcURL4.getParameters());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void taskManagerTest(){
        TaskManager taskManager = context.getBean(TaskManager.class);
        // it should be less than the number of threadpool's threads
        int taskNumber = 32;
        for (int i = 0; i < taskNumber; i++) {
            final int idx = i;
            new Thread(()->{
                RpcRequestBody request = new RpcRequestBody();
                request.setService("Task :"+String.valueOf(idx));
                taskManager.submit(request);
            }).start();
        }


        try {
            Thread.sleep(60000);
        }catch (Exception e){

        }
    }

    @Test
    public void testManagerTestError(){
        TaskManager taskManager = context.getBean(TaskManager.class);
        RpcRequestBody request = new RpcRequestBody();
        request.setService("Task :"+String.valueOf(1));
        taskManager.submit(request);
        try {
            Thread.sleep(30000);
        }catch (Exception e){

        }
    }

    @Test
    public void taskManagerUnitTest() throws Exception{
        TaskManager taskManager = context.getBean(TaskManager.class);
        // default version
        RpcRequestBody request = new RpcRequestBody();
        request.setService("com.leggasai.rpc.gsrpcspringboot.api.HelloService");
        request.setMethod("hello");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"leggasai"});
        request.setVersion("");
        CompletableFuture<RpcResponseBody> future = taskManager.submit(request);
        RpcResponseBody responseBody = future.get();
        assert responseBody.getResult().equals(new HelloServiceImpl().hello("leggasai"));

        // 2.0 version
        request.setService("com.leggasai.rpc.gsrpcspringboot.api.HelloService");
        request.setMethod("hello");
        request.setParameterTypes(new Class[]{String.class});
        request.setParameters(new Object[]{"leggasai"});
        request.setVersion("2.0");
        CompletableFuture<RpcResponseBody> future1 = taskManager.submit(request);
        RpcResponseBody responseBody1 = future1.get();
        assert responseBody1.getResult().equals(new HelloServiceImplV2().hello("leggasai"));

        // no such service error
        request.setService("unknown Service");
        request.setMethod("hello");
        CompletableFuture<RpcResponseBody> future2 = taskManager.submit(request);
        RpcResponseBody responseBody2 = future2.get();
        RpcException result = (RpcException) responseBody2.getResult();
        assert result.getCode() == ErrorCode.SERVICE_NOT_FOUND.getCode();

        // no such method error
        request.setService("com.leggasai.rpc.gsrpcspringboot.api.HelloService");
        request.setMethod("unknown method");
        CompletableFuture<RpcResponseBody> future3 = taskManager.submit(request);
        RpcResponseBody responseBody3 = future3.get();
        RpcException result1 = (RpcException) responseBody3.getResult();
        assert result1.getCode() == ErrorCode.METHOD_NOT_FOUND.getCode();
    }


    @Test
    public void taskManagerTimeoutTest(){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) CachedThreadPool.getExecutor("test", 5, 5, 60 * 1000);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                while (!Thread.interrupted()){

                }
                System.out.println("执行完成");
                return "finish";
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }, executor);

        System.out.println(future.isDone());

        try {
            Thread.sleep(5000);
        }catch (Exception e) {

        }
        future.cancel(true);
        System.out.println("超时取消");
        System.out.println(future.isCancelled());
        try {
            Thread.sleep(1000);
        }catch (Exception e) {

        }
        System.out.println(executor.getActiveCount());
        try {
            Thread.sleep(10000);
        }catch (Exception e) {

        }
        System.out.println(executor.getActiveCount());


    }

    @Test
    public void nettyTest() throws Exception {

    }

    @Test
    public void kindredTest(){
        // 默认
        Kindred kindred = new Kindred();
        printKindred(kindred);

        // 请求ID
        Long id = Snowflake.generateId();
        Kindred kindred1 = new Kindred(id);
        printKindred(kindred1);
        assert kindred1.getRequestId().equals(id);

        // Request
        Kindred kindred2 = new Kindred();
        kindred2.setRequest();
        assert kindred2.isRequest();
        kindred2.setNoData();
        assert !kindred2.needReturnData();
        kindred2.setNoEvent();
        assert !kindred2.isEvent();
        kindred2.setSerialize(SerializationType.FSTSERIALIZE);
        kindred2.setStatus(ErrorCode.NULL);
        kindred2.setLength(16);
        printKindred(kindred2);
        // Response
        kindred2.setResponse();
        assert !kindred2.isRequest();
        kindred2.setNeedData();
        assert kindred2.needReturnData();
        kindred2.setEvent();
        assert kindred2.isEvent();
        kindred2.setSerialize(SerializationType.PROTOSTUFFSERIALIZE);
        kindred2.setStatus(ErrorCode.OK);
        printKindred(kindred2);

        // Body
        kindred2.setResponseBody(RpcResponseBody.successWithResult("ok"));
        RpcRequestBody requestBody = new RpcRequestBody();
        requestBody.setService("com.rpc.demo.hello");
        requestBody.setMethod("sayHello");
        kindred2.setRequestBody(requestBody);
        printKindred(kindred2);
    }
    private void printKindred(Kindred kindred){
        System.out.println("-----------Kindred-----------");
        System.out.println("RequestId:"+kindred.getRequestId());
        System.out.println("Request/Response:"+kindred.isRequest());
        System.out.println("NeedData:"+kindred.needReturnData());
        System.out.println("IsEvent:"+kindred.isEvent());
        System.out.println("Serialization:"+ SerializationType.getBySerializeId((int)kindred.getSerializeId()).getSerializeProtocol());
        System.out.println("Status:"+ErrorCode.getByCode(kindred.getStatus()));
        System.out.println("Length:"+kindred.getLength());
        System.out.println("Request:"+kindred.getRequestBody());
        System.out.println("Response:"+kindred.getResponseBody());
    }



    @Test
    public void proxyTest(){
        DemoService demoService = context.getBean(DemoService.class);
        demoService.demoTest();

    }

    @Test
    public void netTest(){
        System.out.println(NetUtil.getLocalAddress());
        System.out.println(NetUtil.getLocalHost());
        System.out.println(NetUtil.getLocalHostName());
        try {
            Thread.sleep(5000);
        }catch (InterruptedException e){}
    }



    @Test
    public void connectionPoolTest(){
        ConnectionPoolManager connectionPoolManager = context.getBean(ConnectionPoolManager.class);
        DiscoveryCenter discoveryCenter = context.getBean(DiscoveryCenter.class);
        AbstractClientChannelHandler managerHandler = connectionPoolManager.getHandler(new Invoker("192.168.241.1:20888"));
        System.out.println(managerHandler);
    }

    @Test
    public void connectionPoolSystemTest(){
        ConnectionPoolManager connectionPoolManager = context.getBean(ConnectionPoolManager.class);
        AbstractClientChannelHandler managerHandler = connectionPoolManager.getHandler(new Invoker("192.168.241.1:20888"));
    }

    @Test
    public void systemTest(){
        DemoService demoService = context.getBean(DemoService.class);

        CacheService cacheService = context.getBean(CacheService.class);

        System.out.println(System.currentTimeMillis());
        demoService.helloTest("123");
        System.out.println(System.currentTimeMillis());
        System.out.println(System.currentTimeMillis());
        demoService.helloTest("123");
        System.out.println(System.currentTimeMillis());

    }

    @Test
    public void dtoTest(){
        DemoService demoService = context.getBean(DemoService.class);
        Order order = new Order();
        order.setOrderId(123456L);
        HashSet<String> set = new HashSet<>();
        set.add("商品1");
        set.add("商品2");
        order.setProductsSet(set);

        HashMap<String, Integer> map = new HashMap<>();
        map.put("商品1", 1);
        map.put("商品2", 2);
        order.setProductsCountMap(map);

        order.setProductsList(Arrays.asList("商品1","商品2"));
        order.setCreateTime(new Date());

        RpcSerialization serialize = SerializationFactory.getSerialize(SerializationType.KRYOSERIALIZE);
        byte[] bytes = serialize.serialize(order);
        Object deserialize = serialize.deserialize(bytes, Order.class);
        System.out.println(deserialize);
        //long start = System.currentTimeMillis();
        //Order result = demoService.getOrder(order);
        //long end = System.currentTimeMillis();
        //System.out.println(result);
        //System.out.println("耗时:"+(end-start));
    }



    @Test
    public void concurrentTest(){
        DemoService demoService = context.getBean(DemoService.class);
        HelloServiceImpl helloService = context.getBean(HelloServiceImpl.class);
        HelloServiceImplV2 helloService2 = context.getBean(HelloServiceImplV2.class);
        ExecutorService executorService = Executors.newFixedThreadPool(1000);

        // 32个线程，同时调用1000次方法 demoService.test()
        int threadCount = 32;
        int invokeCount = 1000;

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(()->{
                long start = System.currentTimeMillis();
                for (int j = 0; j < invokeCount; j++) {
                    String rpcResult = demoService.helloTest(String.valueOf(j));
                    String localResult = helloService.hello(String.valueOf(j));
                    assert rpcResult.equals(localResult);
                }
                long end = System.currentTimeMillis();
                System.out.println(String.format("Thread{%d} start at {%s} ,end at {%s}, cost:{%d}, ops:{%d}",index,start,end,end-start,invokeCount*1000/(end-start)));
            });
        }
        executorService.shutdown(); // 关闭线程池的提交
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // 等待所有任务执行完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 重新设置中断状态
        }
    }

    @Test
    public void heartBeatTest(){
        try {
            // we can see client send heartbeat 4 times and server ack 4 times(1 time for startup and 3 times for idle-check)
            // Extend by 15 seconds to avoid the test ending before receiving the third heartbeat.
            Thread.sleep(HeartBeat.HEARTBEAT_INTERVAL * 3 + 15);
        }catch (InterruptedException e){

        }
    }



}
