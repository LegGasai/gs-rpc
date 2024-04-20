package com.leggasai.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.leggasai.rpc.serialization.RpcSerialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerialization implements RpcSerialization {

    @Override
    public <T> byte[] serialize(T obj) {
        Kryo kryo = KryoPoolFactory.borrow();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output ko = new Output(bos);
        try{
            kryo.writeClassAndObject(ko,obj);
            ko.close();
            return bos.toByteArray();
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            try {
                KryoPoolFactory.release(kryo);
                bos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        Kryo kryo = KryoPoolFactory.borrow();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Input ki = new Input(bis);
        try{
            Object object = kryo.readClassAndObject(ki);
            ki.close();
            return object;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            try {
                bis.close();
                KryoPoolFactory.release(kryo);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
