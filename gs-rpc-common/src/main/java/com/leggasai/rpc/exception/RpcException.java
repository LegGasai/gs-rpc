package com.leggasai.rpc.exception;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-04-13:25
 * @Description: 自定义RPC异常
 */
public class RpcException extends RuntimeException{

    private int code = ErrorCode.SERVER_ERROR.getCode();
    private String message;

    public RpcException() {
        super();
    }

    public RpcException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public RpcException(String message) {
        super(message);
        this.message = message;
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
    }
    public RpcException(int code,String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
