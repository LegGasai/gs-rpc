package com.leggasai.rpc.serialization.protostuff;

import com.leggasai.rpc.serialization.RpcSerialization;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffSerialization implements RpcSerialization {
    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);




    @Override
    public <T> byte[] serialize(T obj) {
        Schema<T> schema = ProtostuffCache.getSchema((Class<T>)obj.getClass());
        byte[] data;
        try {
            data = ProtobufIOUtil.toByteArray(obj, schema, buffer);
            return data;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            buffer.clear();
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = ProtostuffCache.getSchema(clazz);
        T t = schema.newMessage();
        try {
            ProtobufIOUtil.mergeFrom(bytes, t, schema);
            return t;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
