package com.leggasai.rpc.server.service;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.constants.Separator;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.threadpool.FixedThreadPool;
import com.leggasai.rpc.threadpool.ScheduledThreadPool;

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
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) FixedThreadPool.getExecutor("TaskManager",200,0);

    private final AtomicInteger taskCount = new AtomicInteger(0);

    //private final ConcurrentHashMap<CompletableFuture<RpcResponseBody>,Long> pendingTasks = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor)ScheduledThreadPool.getExecutor("TaskManager-Schedule",8);

    public CompletableFuture<RpcResponseBody> submit(RpcRequestBody request){
        try {
            taskCount.incrementAndGet();
            //CompletableFuture<RpcResponseBody> task = CompletableFuture.supplyAsync(() -> {
            //    try {
            //        return handle(request);
            //    } catch (RpcException e) {
            //        logger.error("TaskManager execute error", e);
            //        return RpcResponseBody.failWithException(e);
            //    }
            //}, executor);
            //return task;
            CompletableFuture<RpcResponseBody> task = new CompletableFuture<>();
            // 任务执行超时监听
            ScheduledFuture<?> scheduledFuture = scheduler.schedule(() -> {
                if (!task.isDone()) {
                    logger.warn("TaskManager task execution may timeout in {} ms and be cancelled",providerProperties.getTimeout());
                    task.complete(RpcResponseBody.failWithException(new RpcException(ErrorCode.SERVER_TIMEOUT.getCode(), ErrorCode.SERVER_TIMEOUT.getMessage())));
                }
            }, providerProperties.getTimeout(), TimeUnit.MILLISECONDS);

            // 任务完成监听
            task.whenComplete((response, throwable) -> {
                scheduledFuture.cancel(false);
                taskCount.decrementAndGet();
            });
            // 任务执行——线程池
            executeWithRetry(task, request, providerProperties.getRetries(), null);
            return task;
        }catch (RejectedExecutionException e){
            RpcException exception = new RpcException(ErrorCode.SERVER_LIMIT_RATE.getCode(), ErrorCode.SERVER_LIMIT_RATE.getMessage());
            logger.error("TaskManager exceed limit",e);
            taskCount.decrementAndGet();
            return CompletableFuture.completedFuture(RpcResponseBody.failWithException(exception));
        }finally {

        }
    }

    private void executeWithRetry(CompletableFuture<RpcResponseBody> task, RpcRequestBody request, int retry, RpcException lastException){
        executor.execute(()->{
            if (retry < 0){
                logger.error("TaskManager task execution fails after {} retries, the last exception is",providerProperties.getRetries(),lastException);
                task.complete(RpcResponseBody.failWithException(lastException));
                return;
            }
            try{
                RpcResponseBody responseBody = handle(request);
                task.complete(responseBody);
            }catch (RpcException e){
                logger.error("TaskManager task execution error with retry :{}", retry,e);
                executeWithRetry(task,request,retry-1,e);
            }
        });
    }

    /**
     * 执行Request
     * @param request
     * @return
     * @throws RpcException:封装了执行时的各种异常
     */
    private RpcResponseBody handle(RpcRequestBody request) throws RpcException{
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
        try {
            FastClass serviceFastClass = serviceManager.getServiceFastClass(serviceKey);
            FastMethod serviceFastMethod = serviceFastClass.getMethod(method, parameterTypes);
            Object result = serviceFastMethod.invoke(serviceImpl, parameters);
            if (serviceFastMethod.getReturnType() == Void.class){
                return RpcResponseBody.successWithNull();
            }else{
                return RpcResponseBody.successWithResult(result);
            }
        } catch (NoSuchMethodError e){
            throw new RpcException(ErrorCode.METHOD_NOT_FOUND.getCode(),ErrorCode.METHOD_NOT_FOUND.getMessage(),e);
        } catch (InvocationTargetException e) {
            throw new RpcException(ErrorCode.SERVICE_ERROR.getCode(), ErrorCode.SERVICE_ERROR.getMessage(),e.getTargetException());
        } catch (Exception e){
            // 异常兜底
            throw new RpcException(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMessage(),e);
        }

    }

    public void shutdown(){
        executor.shutdown();
        scheduler.shutdown();
        if (taskCount.get() > 0){
            logger.warn("TaskManager waiting for shutdown..., and the number of running tasks is {}", taskCount.get());
            try {
                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    logger.info("TaskManager time out shutdown, the number of unfinished tasks is {}", taskCount.get());
                    executor.shutdownNow();
                    scheduler.shutdownNow();
                }else{
                    logger.info("TaskManager has shutdown successfully after all tasks finished.");
                }
            } catch (InterruptedException e) {
                logger.info("TaskManager shutdown error and is forcing to shutdown, the number of unfinished tasks is {}", taskCount.get());
                executor.shutdownNow();
                scheduler.shutdownNow();
            }
        }else{
            logger.info("TaskManager has shutdown successfully after all tasks finished.");
        }
    }

}
