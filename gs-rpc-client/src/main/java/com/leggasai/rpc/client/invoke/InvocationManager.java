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
            future.complete(response.getResult());
            Invocation invocation = invocationMap.get(requestId);
            // todo 将invocatio发送给StatisticsCenter统计调用信息
            pendingRpcs.remove(requestId,future);
            invocationMap.remove(requestId,invocation);
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
        executor.execute(() -> executeInvocation(request, future));
        return future;
    }

    private void executeInvocation(RpcRequestBody request,CompletableFuture<Object> future){
        Invocation invocation = new Invocation(request);
        invocation.setStartTime(System.currentTimeMillis());
        invocation.setSerializationType(SerializationType.getByProtocol(consumerProperties.getSerialization()));
        pendingRpcs.put(invocation.getRequestId(),future);
        invocationMap.put(invocation.getRequestId(),invocation);
        // 负载均衡 选出handler invocation.setHandler
        String serviceKey = request.getService() + Separator.SERVICE_SPLIT + request.getVersion();
        List<Invoker> invokers = discoveryCenter.getInvokersByServiceKey(serviceKey);
        if (CollectionUtils.isEmpty(invokers)){
            logger.error("InvocationManager: No invokers found for serviceKey:{}",serviceKey);
            future.completeExceptionally(new RpcException(ErrorCode.SERVICE_ERROR.getCode(), ErrorCode.SERVICE_ERROR.getMessage()));
            pendingRpcs.remove(invocation.getRequestId(),future);
            invocationMap.remove(invocation.getRequestId(),invocation);
            return;
        }
        LoadBalance loadBalance = LoadBalanceFactory.getLoadBalance(LoadBalanceType.getByType(consumerProperties.getLoadBalance()));
        Invoker invoker = loadBalance.select(invokers,invocation);
        invocation.setInvoker(invoker);
        AbstractClientChannelHandler channelHandler = connectionPoolManager.getHandler(invoker);
        channelHandler.invoke(invocation);
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
