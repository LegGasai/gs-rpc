package com.leggasai.rpc.server.service;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.constants.Separator;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import com.leggasai.rpc.threadpool.ScheduledThreadPool;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-22:02
 * @Description: 任务调度中心 unthreadsafe
 */
@Component
public class TaskManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    @Autowired
    private ProviderProperties providerProperties;
    @Autowired
    private ServiceManager serviceManager;
    /**
     * 优雅停机等待时间
     */
    private final static int SHUTDOWN_TIMEOUT = 10 * 1000;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) CachedThreadPool.getExecutor("test-demo",16,16,60 * 1000);
    private final AtomicInteger taskCount = new AtomicInteger(0);
    //private final ConcurrentHashMap<CompletableFuture<RpcResponseBody>,Long> pendingTasks = new ConcurrentHashMap<>();
    //private final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)ScheduledThreadPool.getExecutor("test-demo",1);
    // 超时机制 todo
    public CompletableFuture<RpcResponseBody> submit(RpcRequestBody request){
        try {
            taskCount.incrementAndGet();
            CompletableFuture<RpcResponseBody> task = CompletableFuture.supplyAsync(() -> {
                try {
                    return handle(request);
                } catch (RpcException e) {
                    logger.error("TaskManager execute error", e);
                    return RpcResponseBody.failWithException(e);
                }
            }, executor);
            // pendingTasks.put(task,System.currentTimeMillis());
            return task;
        }catch (RejectedExecutionException e){
            RpcException exception = new RpcException(ErrorCode.SERVER_LIMIT_RATE.getCode(), ErrorCode.SERVER_LIMIT_RATE.getMessage());
            logger.error("TaskManager exceed limit",e);
            return CompletableFuture.completedFuture(RpcResponseBody.failWithException(exception));
        }finally {
            taskCount.decrementAndGet();
        }



    }

    private RpcResponseBody handle(RpcRequestBody request) throws RpcException{
        // 这里需要抛出各种异常
        // Thread.sleep(5000);
        if (request == null){
            throw new RpcException(ErrorCode.SERVER_ERROR.getCode(),ErrorCode.SERVER_ERROR.getMessage());
        }
        String serviceName = request.getService();
        String version = request.getVersion();
        String serviceKey = serviceName + Separator.SERVICE_SPLIT + version;
        Object serviceImpl = serviceManager.getService(serviceKey);
        if (serviceImpl == null){
            throw new RpcException(ErrorCode.SERVICE_NOT_FOUND.getCode(),ErrorCode.SERVICE_NOT_FOUND.getMessage());
        }
        String method = request.getMethod();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        Class<?> serviceClazz = serviceImpl.getClass();
        try {
            FastClass serviceFastClass = FastClass.create(serviceClazz);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(method, parameterTypes);
            Object result = serviceFastMethod.invoke(serviceImpl, parameters);
            if (serviceFastMethod.getReturnType() == Void.class){
                return RpcResponseBody.successWithNull();
            }else{
                return RpcResponseBody.successWithResult(result);
            }
        } catch (NoSuchMethodError e){
            throw new RpcException(ErrorCode.METHOD_NOT_FOUND.getCode(),ErrorCode.METHOD_NOT_FOUND.getMessage(),e.getCause());
        } catch (InvocationTargetException e) {
            throw new RpcException(ErrorCode.SERVICE_ERROR.getCode(), ErrorCode.SERVICE_ERROR.getMessage(),e.getTargetException());
        }


        //System.out.println(request);
        //throw new RpcException(ErrorCode.SERVER_TIMEOUT.getCode(),ErrorCode.SERVER_TIMEOUT.getMessage());
        // return null;
    }

    public void shutdown(){
        executor.shutdown();
        if (taskCount.get() > 0){
            logger.warn("TaskManager waiting for shutdown..., and the number of running tasks is {}", taskCount.get());
            try {
                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    logger.info("TaskManager time out shutdown, the number of unfinished tasks is {}", taskCount.get());
                    executor.shutdownNow();
                }else{
                    logger.info("TaskManager has shutdown successfully after all tasks finished.");
                }
            } catch (InterruptedException e) {
                logger.info("TaskManager shutdown error and is forcing to shutdown, the number of unfinished tasks is {}", taskCount.get());
                executor.shutdownNow();
            }
        }else{
            logger.info("TaskManager has shutdown successfully after all tasks finished.");
        }
    }

}
