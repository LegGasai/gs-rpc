package com.leggasai.rpc.serialization;

public interface RpcSerialization {
    <T> byte[] serialize(T obj);

    <T> Object deserialize(byte[] bytes, Class<T> clazz);
}
