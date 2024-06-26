package com.leggasai.rpc.client.invoke;

import com.leggasai.rpc.client.discovery.DiscoveryCenter;
import com.leggasai.rpc.client.netty.ConnectionPoolManager;
import com.leggasai.rpc.client.netty.handler.AbstractClientChannelHandler;
import com.leggasai.rpc.client.route.loadBalance.LoadBalance;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceFactory;
import com.leggasai.rpc.client.route.loadBalance.LoadBalanceType;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.constants.Separator;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.serialization.SerializationType;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import com.leggasai.rpc.threadpool.FixedThreadPool;
import io.protostuff.Rpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-09-12:20
 * @Description: 服务调用者RPC调用管理中心
 */
@Component
public class InvocationManager {

    private static final Logger logger = LoggerFactory.getLogger(InvocationManager.class);
    /**
     * 线程池
     */
    //private final ThreadPoolExecutor executor = (ThreadPoolExecutor) CachedThreadPool.getExecutor("InvocationManager",128,256,60 * 1000,0);
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) FixedThreadPool.getExecutor("InvocationManager",256,0);

    /**
     * 等待响应的RPC调用
     */
    private final Map<Long, CompletableFuture<Object>> pendingRpcs = new ConcurrentHashMap<>();
    /**
     * 重试次数
     */
    private final Map<Long, Integer> invokeCountMap = new ConcurrentHashMap<>();

    /**
     * 调用信息Map
     */
    private final Map<Long, Invocation> invocationMap = new ConcurrentHashMap<>();

    @Autowired
    private ConnectionPoolManager connectionPoolManager;

    @Autowired
    private DiscoveryCenter discoveryCenter;

    @Autowired
    private ConsumerProperties consumerProperties;
    /**
     * rpc完成, unThreadsafe
     * @param requestId
     */
    public void markFinish(Long requestId, RpcResponseBody response){
        if (requestId != null && pendingRpcs.containsKey(requestId)){
            CompletableFuture<Object> future = pendingRpcs.get(requestId);
            Invocation invocation = invocationMap.get(requestId);
            if (!response.isSuccess()){
                Integer retryCount = invokeCountMap.getOrDefault(requestId, 0);
                if (invokeCountMap.getOrDefault(requestId,0) > 0){
                    logger.warn("Invocation Manager: RPC invocation fails, and try to retry with retries:{}, RequestId:{}",retryCount,requestId);
                    invokeCountMap.put(requestId,retryCount -1 );
                    executor.execute(() -> executeInvocation(invocation, future));
                    return;
                }else{
                    logger.warn("Invocation Manager: RPC invocation fails, after {} retries, RequestId:{}",consumerProperties.getRetries(),requestId);
                }
            }
            invocationFinish(invocation,future,response,null);
            // todo 将invocatio发送给StatisticsCenter统计调用信息
            logger.debug("Invocation Manager: RPC requestId :{} has finished with response:{}, cost time:{} ms",requestId,response,invocation.getCostTime());
        }else{
            logger.error("Invocation Manager has no such RPC requestId :{}", requestId);
        }
    }

    /**
     * 提交请求并返回一个CompletableFuture对象
     * @param request
     * @return
     */
    public CompletableFuture<Object> submitRequest(RpcRequestBody request){
        CompletableFuture<Object> future = new CompletableFuture<>();
        Invocation invocation = invocationStart(request, future);
        executor.execute(() -> executeInvocation(invocation, future));
        return future;
    }

    private void executeInvocation(Invocation invocation,CompletableFuture<Object> future){
        RpcRequestBody request = invocation.getRequest();
        // 负载均衡 选出handler invocation.setHandler
        String serviceKey = request.getService() + Separator.SERVICE_SPLIT + request.getVersion();
        List<Invoker> invokers = discoveryCenter.getInvokersByServiceKey(serviceKey);
        if (CollectionUtils.isEmpty(invokers)){
            logger.error("InvocationManager: No invokers found for serviceKey:{}",serviceKey);
            RpcException exception = new RpcException(ErrorCode.SERVICE_ERROR.getCode(), ErrorCode.SERVICE_ERROR.getMessage());
            invocationFinish(invocation,future,null,exception);
            return;
        }
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.getByType(consumerProperties.getLoadBalance()));
        Invoker invoker = loadBalance.select(invokers,invocation);
        invocation.setInvoker(invoker);
        AbstractClientChannelHandler channelHandler = connectionPoolManager.getHandler(invoker);
        channelHandler.invoke(invocation);
    }

    private Invocation invocationStart(RpcRequestBody request, CompletableFuture<Object> task){
        Invocation invocation = new Invocation(request);
        invocation.setStartTime(System.currentTimeMillis());
        invocation.setSerializationType(SerializationType.getByProtocol(consumerProperties.getSerialization()));
        pendingRpcs.put(invocation.getRequestId(),task);
        invocationMap.put(invocation.getRequestId(),invocation);
        invokeCountMap.put(invocation.getRequestId(),consumerProperties.getRetries());
        return invocation;
    }

    private void invocationFinish(Invocation invocation, CompletableFuture<Object> future, RpcResponseBody response, Exception exception){
        if (response == null) {
            if (exception != null) {
                future.completeExceptionally(new RpcException(ErrorCode.SERVICE_ERROR.getCode(), ErrorCode.SERVICE_ERROR.getMessage()));
            }
            invocation.setSuccess(false);
        }else{
            future.complete(response.getResult());
            invocation.setSuccess(response.isSuccess());
        }

        invocation.setEndTime(System.currentTimeMillis());
        pendingRpcs.remove(invocation.getRequestId(),future);
        invocationMap.remove(invocation.getRequestId(),invocation);
        invocationMap.remove(invocation.getRequestId());
    }

    /**
     * 关闭调用中心
     * 这里不必等待线程池任务结束，既然客户端需要关闭。
     */
    public void shutdown(){
        executor.shutdown();
        logger.info("Invocation Manager has shutdown");
    }
}
