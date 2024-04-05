package com.leggasai.rpc.codec;

import com.leggasai.rpc.enums.ResponseType;

import java.io.Serializable;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:17
 * @Description: Response响应体
 */
public class RpcResponseBody implements Serializable {

    /**
     * rpc响应类型
     */
    private ResponseType responseType;
    /**
     * rpc响应结果
     */
    private Object result;

    public RpcResponseBody() {
    }

    public RpcResponseBody(ResponseType responseType) {
        this.responseType = responseType;
    }

    public RpcResponseBody(ResponseType responseType, Object result) {
        this.responseType = responseType;
        this.result = result;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }


    public static RpcResponseBody successWithResult(Object result) {
        return new RpcResponseBody(ResponseType.RESPONSE_VALUE, result);
    }

    public static RpcResponseBody successWithNull() {
        return new RpcResponseBody(ResponseType.RESPONSE_NULL);
    }

    public static RpcResponseBody failWithException(Object exception) {
        return new RpcResponseBody(ResponseType.RESPONSE_ERROR, exception);
    }

    /**
     * just for debug, fixme
     * @return
     */
    @Override
    public String toString() {
        return "RpcResponseBody{" +
                "responseType=" + responseType +
                ", result=" + result +
                '}';
    }
}
