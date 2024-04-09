package com.leggasai.rpc.client.invoke;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.enums.ResponseType;
import com.leggasai.rpc.utils.Snowflake;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-10:19
 * @Description: RPC调用信息，用于统计中心统计
 */
public class Invocation {
    private RpcRequestBody request;
    private Invoker invoker;
    private long startTime;
    private long endTime;
    private boolean isSuccess;

    private Long requestId;

    public Invocation(RpcRequestBody request) {
        this.request = request;
        this.requestId = Snowflake.generateId();
    }

    public Invocation() {
        this.requestId = Snowflake.generateId();
    }

    public RpcRequestBody getRequest() {
        return request;
    }

    public void setRequest(RpcRequestBody request) {
        this.request = request;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public long getCostTime() {
        return Math.max(endTime - startTime,0);
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }


    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public Long getRequestId() {
        return requestId;
    }
}
