package com.leggasai.rpc.client.proxy.invoke;


import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-06-12:14
 * @Description: RPC方法执行,封装客户端执行rpc调用的统一逻辑
 */
public class RpcMethodInvoke implements MethodInvoke{

    private static final Logger logger = LoggerFactory.getLogger(RpcMethodInvoke.class);
    private final String service;
    private final String version;


    public RpcMethodInvoke(String service, String version) {
        this.service = service;
        this.version = version;
    }

    private InvocationManager invocationManager;

    private ConsumerProperties consumerProperties;

    public void setInvocationManager(InvocationManager invocationManager) {
        this.invocationManager = invocationManager;
    }

    public void setConsumerProperties(ConsumerProperties consumerProperties) {
        this.consumerProperties = consumerProperties;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequestBody requestBody = new RpcRequestBody();
        requestBody.setService(service);
        requestBody.setVersion(version);
        requestBody.setMethod(method.getName());
        requestBody.setParameters(args);
        requestBody.setParameterTypes(method.getParameterTypes());
        CompletableFuture<Object> future = invocationManager.submitRequest(requestBody);
        try {
            Object result = future.get(consumerProperties.getTimeout(), TimeUnit.MILLISECONDS);
            if (result instanceof RpcException){
                logger.error("RPC invoke error，ServiceKey={}#{}, Method={} ,{}",service,version,method.getName(),result);
                throw (RpcException) result;
            } else{
                logger.info("RPC invoke success，ServiceKey={}#{}, Method={} ,result = {}",service,version,method.getName(),result);
                return result;
            }
        }catch (TimeoutException e){
            logger.error("RPC invoke timeout in {} seconds，ServiceKey={}#{}, Method={} ,",consumerProperties.getTimeout()/1000, service, version, method.getName(), e);
            throw new RpcException(ErrorCode.CLIENT_TIMEOUT.getCode(),ErrorCode.CLIENT_TIMEOUT.getMessage());
        }catch (ExecutionException  | InterruptedException e){
            logger.error("RPC invoke error，ServiceKey={}#{}, Method={} ,",service,version,method.getName(),e);
            throw e;
        }
    }

}
