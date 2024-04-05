package com.leggasai.rpc.protocol;

import com.leggasai.rpc.serialization.SerializeType;

public enum ProtocolType {
    KINDRED("kindred");
    private final String name;

    ProtocolType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ProtocolType getByName(String protocol){
        switch (protocol){
            case "kindred":
                return KINDRED;
            default:
                throw new RuntimeException("不支持的RPC协议");
        }
    }
}
