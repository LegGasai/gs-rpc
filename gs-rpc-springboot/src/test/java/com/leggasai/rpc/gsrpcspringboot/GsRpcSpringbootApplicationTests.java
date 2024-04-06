package com.leggasai.rpc.gsrpcspringboot;

import com.leggasai.rpc.client.discovery.DiscoveryBeanPostProcessor;
import com.leggasai.rpc.client.discovery.DiscoveryCenter;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.common.beans.RpcURL;
import com.leggasai.rpc.common.beans.ServiceMeta;
import com.leggasai.rpc.config.ApplicationProperties;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.config.RegistryProperties;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.gsrpcspringboot.provider.impl.HelloServiceImpl;
import com.leggasai.rpc.gsrpcspringboot.provider.impl.HelloServiceImplV2;
import com.leggasai.rpc.server.registry.RegistryCenter;
import com.leggasai.rpc.server.service.ServiceManager;
import com.leggasai.rpc.server.service.TaskManager;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import com.leggasai.rpc.threadpool.FixedThreadPool;
import com.leggasai.rpc.utils.Snowflake;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
}
