package com.leggasai.rpc.serialization.protostuff;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-22:05
 * @Description:
 */
public class ProtostuffCache {
    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<Class<?>, Schema<?>>();

    public static <T> Schema<T> getSchema(Class<T> cls) {
        // for thread-safe
        return (Schema<T>) schemaCache.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }

    static {
        schemaCache.put(RpcRequestBody.class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(RpcResponseBody.class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(HashMap.class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(Object[].class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(Class[].class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(String[].class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(Object.class,RuntimeSchema.createFrom(RpcRequestBody.class));
        schemaCache.put(String.class,RuntimeSchema.createFrom(RpcRequestBody.class));
    }
}
