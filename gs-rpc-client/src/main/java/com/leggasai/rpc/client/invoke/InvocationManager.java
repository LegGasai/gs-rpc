package com.leggasai.rpc.client.invoke;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import io.protostuff.Rpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

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
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) CachedThreadPool.getExecutor("invocation-manager",16,16,60 * 1000);

    /**
     * 等待响应的RPC调用
     */
    private final Map<Long, CompletableFuture<Object>> pendingRpcs = new ConcurrentHashMap<>();

    /**
     * 调用信息Map
     */
    private final Map<Long, Invocation> invocationMap = new ConcurrentHashMap<>();
    /**
     * rpc完成, unThreadsafe
     * @param requestId
     */
    public void markFinish(Long requestId, RpcResponseBody response){
        if (requestId != null && pendingRpcs.containsKey(requestId)){
            CompletableFuture<Object> future = pendingRpcs.get(requestId);
            future.complete(response.getResult());
            Invocation invocation = invocationMap.get(requestId);
            invocation.setEndTime(System.currentTimeMillis());
            // todo 将invocatio发送给StatisticsCenter统计调用信息
            pendingRpcs.remove(requestId,future);
            invocationMap.remove(requestId,invocation);
            logger.info("Invocation Manager: RPC requestId :{} has finished with response:{}",requestId,response);
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
        pendingRpcs.put(invocation.getRequestId(),future);
        invocationMap.put(invocation.getRequestId(),invocation);
        // 负载均衡 选出handler invocation.setHandler

        // 获取handler 并 向handler发送request

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
