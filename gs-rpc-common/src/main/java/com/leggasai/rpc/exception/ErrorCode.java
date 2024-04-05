package com.leggasai.rpc.exception;

public enum ErrorCode {
    OK(20,"调用成功"),
    CLIENT_TIMEOUT(31,"客户端超时"),
    SERVER_TIMEOUT(32,"服务端调用超时"),
    SERVER_ERROR(40,"服务端发生异常"),
    SERVER_LIMIT_RATE(50,"服务端限流,请稍后再试"),
    METHOD_NOT_FOUND(60,"服务端调用方法不存在"),
    SERVICE_NOT_FOUND(70,"服务端服务不存在"),
    SERVICE_ERROR(80,"服务调用异常");

    private int code;
    private String message;

    ErrorCode(int code, String message) {
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
