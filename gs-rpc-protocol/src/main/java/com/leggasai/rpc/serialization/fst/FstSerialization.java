package com.leggasai.rpc.serialization.fst;

import com.leggasai.rpc.serialization.RpcSerialization;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FstSerialization implements RpcSerialization {
    static FSTConfiguration conf = FstConfigFactory.getInstance();
    @Override
    public <T> byte[] serialize(T obj) {
        return conf.asByteArray(obj);
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        return conf.asObject(bytes);
    }
}
