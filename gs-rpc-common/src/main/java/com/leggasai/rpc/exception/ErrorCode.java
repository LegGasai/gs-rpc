package com.leggasai.rpc.exception;

public enum ErrorCode {
    NULL(00,"Request默认错误码"),
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

    public static ErrorCode getByCode(int code){
        switch (code){
            case 00:
                return NULL;
            case 20:
                return OK;
            case 31:
                return CLIENT_TIMEOUT;
            case 32:
                return SERVER_TIMEOUT;
            case 40:
                return SERVER_ERROR;
            case 50:
                return SERVER_LIMIT_RATE;
            case 60:
                return METHOD_NOT_FOUND;
            case 70:
                return SERVICE_NOT_FOUND;
            case 80:
                return SERVICE_ERROR;
            default:
                throw new RuntimeException("无对应的ErrorCode, code = " + code);
        }
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
