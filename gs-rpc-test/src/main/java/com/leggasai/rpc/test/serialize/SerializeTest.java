package com.leggasai.rpc.test.serialize;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.serialization.RpcSerialization;
import com.leggasai.rpc.serialization.SerializationFactory;
import com.leggasai.rpc.serialization.SerializationType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-21:09
 * @Description:
 */
public class SerializeTest {


    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            benchmark(10000, SerializationType.KRYOSERIALIZE);
        }
        for (int i = 0; i < 10; i++) {
            benchmark(10000, SerializationType.FSTSERIALIZE);
        }
        for (int i = 0; i < 10; i++) {
            benchmark(10000, SerializationType.HESSIANSERIALIZE);
        }
        for (int i = 0; i < 10; i++) {
            benchmark(10000, SerializationType.PROTOSTUFFSERIALIZE);
        }
        for (int i = 0; i < 10; i++) {
            benchmark(10000, SerializationType.JDKSERIALIZE);
        }
    }

    public static void benchmark(int count, SerializationType type){
        RpcSerialization serialize = SerializationFactory.getSerialize(type);
        long totalBytes = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            RpcRequestBody body = mockRequestBody();
            byte[] bytes = serialize.serialize(body);
            totalBytes += bytes.length;
            RpcRequestBody deserializeBody = (RpcRequestBody)serialize.deserialize(bytes,RpcRequestBody.class);
            if (!isEqual(body,deserializeBody)){
                throw new RuntimeException("序列化对象发生改变！");
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("============================");
        System.out.println(String.format("方法：%s 次数：%d 总字节数：%s 耗时：%s",type.name(), count, totalBytes,end - start));
    }

    public static RpcRequestBody mockRequestBody(){

        RpcRequestBody requestBody = new RpcRequestBody();
        requestBody.setService(UUID.randomUUID().toString());
        requestBody.setMethod(UUID.randomUUID().toString());
        requestBody.setVersion(UUID.randomUUID().toString());
        requestBody.setParameterTypes(new Class[]{String.class,Integer.class, Map.class});
        requestBody.setParameters(new Object[]{UUID.randomUUID().toString(), (int)Math.random()*100});
        requestBody.setExtendField(new HashMap<String,Object>());
        return requestBody;
    }

    public static Boolean isEqual(RpcRequestBody original,RpcRequestBody deserialized){
        return original.getService().equals(deserialized.getService()) &&
                original.getMethod().equals(deserialized.getMethod()) &&
                original.getVersion().equals(deserialized.getVersion()) &&
                Arrays.equals(original.getParameterTypes(), deserialized.getParameterTypes()) &&
                Arrays.equals(original.getParameters(), deserialized.getParameters()) &&
                original.getExtendField().equals(deserialized.getExtendField());
    }

}
