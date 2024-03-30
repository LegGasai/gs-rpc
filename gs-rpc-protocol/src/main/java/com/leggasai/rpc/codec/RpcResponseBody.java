package com.leggasai.rpc.codec;

import com.leggasai.rpc.enums.ResponseType;

import java.io.Serializable;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:17
 * @Description:
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
}
