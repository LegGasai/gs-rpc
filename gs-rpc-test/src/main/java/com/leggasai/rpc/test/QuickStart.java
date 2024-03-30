package com.leggasai.rpc.test;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.serialization.fst.FstConfigFactory;
import com.leggasai.rpc.serialization.fst.FstSerialization;
import org.nustaq.serialization.FSTConfiguration;

import java.util.HashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:06
 * @Description:
 */
public class QuickStart {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        RpcRequestBody body = new RpcRequestBody();
        body.setService("com.leggasai.rpc.hello");
        body.setVersion("2.0");
        body.setMethod("hello");
        body.setParameterTypes(new Class[]{String.class});
        body.setParameters(new Object[]{"leggasai"});
        body.setExtendField(new HashMap<String,Object>(){{put("name","leggasai");}});

        FstSerialization fstSerialization = new FstSerialization();
        byte[] bytes = fstSerialization.serialize(body);
        System.out.println(bytes.length);
        RpcRequestBody body1 = (RpcRequestBody) fstSerialization.deserialize(bytes, RpcRequestBody.class);
        System.out.println(body1);
        System.out.println();

    }
}
