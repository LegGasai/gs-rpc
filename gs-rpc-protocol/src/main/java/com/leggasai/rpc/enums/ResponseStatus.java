package com.leggasai.rpc.enums;

import com.leggasai.rpc.codec.RpcResponseBody;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:17
 * @Description: RPC响应返回状态 枚举值
 */
public enum ResponseStatus {

    OK(20,"请求成功"),
    CLIENT_TIMEOUT(31,"客户端超时"),
    SERVER_TIMEOUT(32,"服务端超时"),
    SERVER_ERROR(40,"服务端发生异常"),
    SERVER_LIMIT_RATE(50,"服务端限流,请稍后再试"),
    SERVICE_NOT_FOUND(60,"该调用服务不存在"),
    METHOD_NOT_FOUND(70,"该调用方法不存在"),
    SERVICE_ERROR(80,"服务执行时发生异常"),
    ;

    private int code;
    private String message;

    private ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
