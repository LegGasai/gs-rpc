package com.leggasai.rpc.serialization.fst;

import com.leggasai.rpc.codec.RpcDecoder;
import com.leggasai.rpc.codec.RpcEncode;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.serialization.RpcSerialization;
import com.leggasai.rpc.serialization.SerializationAdapter;
import org.nustaq.serialization.FSTConfiguration;

import java.util.HashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:43
 * @Description:
 */
public class FstConfigFactory {
    private static final FSTConfiguration INSTANCE = FSTConfiguration.createDefaultConfiguration();
    static {
        INSTANCE.registerClass(RpcRequestBody.class);
        INSTANCE.registerClass(RpcResponseBody.class);
        INSTANCE.registerClass(HashMap.class);
        INSTANCE.registerClass(Object[].class);
        INSTANCE.registerClass(Class[].class);
        INSTANCE.registerClass(String[].class);
        INSTANCE.registerClass(Object.class);
        INSTANCE.registerClass(String.class);
    }

    public static FSTConfiguration getInstance() {
        return INSTANCE;
    }
}
