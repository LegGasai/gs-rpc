package com.leggasai.rpc.enums;

import com.leggasai.rpc.codec.RpcResponseBody;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:17
 * @Description: RPC响应返回类型
 * {@link RpcResponseBody}
 */
public enum ResponseType {

    RESPONSE_NULL("null"),
    RESPONSE_VALUE("value"),
    RESPONSE_ERROR("error");
    private String type;

    private ResponseType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ResponseType getByType(String type){
        type = type.toLowerCase();
        switch (type){
            case "null":
                return RESPONSE_NULL;
            case "value":
                return RESPONSE_VALUE;
            case "error":
                return RESPONSE_ERROR;
            default:
                return null;
        }
    }

}
