package com.leggasai.rpc.test.protocol.serialization;

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
        UUID.randomUUID().toString();
        serializeTest(10000,10, SerializationType.KRYOSERIALIZE);
        serializeTest(10000,10, SerializationType.FSTSERIALIZE);
        serializeTest(10000,10, SerializationType.HESSIANSERIALIZE);
        serializeTest(10000,10, SerializationType.PROTOSTUFFSERIALIZE);
        serializeTest(10000,10, SerializationType.JDKSERIALIZE);

    }

    public static void serializeTest(int batch,int count, SerializationType type){
        long totalCost = 0;
        for (int i = 0; i < count; i++) {
            totalCost += benchmark(batch, type);
        }
        System.out.println("==========================测试完成===========================");
        System.out.println(String.format("方法：%s，平均耗时：%d", type.getSerializeProtocol(), totalCost / count));
    }

    public static long singleTest(SerializationType type){

        RpcSerialization serialize = SerializationFactory.getSerialize(type);
        long totalBytes = 0;
        long start = System.currentTimeMillis();
        RpcRequestBody body = mockRequestBody();
        byte[] bytes = serialize.serialize(body);
        totalBytes += bytes.length;
        RpcRequestBody deserializeBody = (RpcRequestBody)serialize.deserialize(bytes,RpcRequestBody.class);
        if (!isEqual(body,deserializeBody)){
            throw new RuntimeException("序列化对象发生改变！");
        }
        long end = System.currentTimeMillis();
            System.out.println("============================");
            System.out.println(String.format("方法：%s 总字节数：%s 耗时：%s",type.name(), totalBytes,end - start));
            return end - start;
        }

    public static long benchmark(int count, SerializationType type){
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
        return end - start;
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
