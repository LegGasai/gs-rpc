package com.leggasai.rpc.serialization;

import com.leggasai.rpc.serialization.fst.FstSerialization;
import com.leggasai.rpc.serialization.hessian.HessianSerialization;
import com.leggasai.rpc.serialization.kryo.KryoSerialization;
import com.leggasai.rpc.serialization.nativejdk.NativeJDKSerialization;
import com.leggasai.rpc.serialization.protostuff.ProtostuffSerialization;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-24-20:02
 * @Description:
 */
public class SerializationFactory {
    public static RpcSerialization getSerialize(SerializeType serializeType){
        switch (serializeType){
            case KRYOSERIALIZE:
                return new KryoSerialization();
            case HESSIANSERIALIZE:
                return new HessianSerialization();
            case JDKSERIALIZE:
                return new NativeJDKSerialization();
            case PROTOSTUFFSERIALIZE:
                return new ProtostuffSerialization();
            case FSTSERIALIZE:
                return new FstSerialization();
            default:
                return new HessianSerialization();
        }
    }
}

