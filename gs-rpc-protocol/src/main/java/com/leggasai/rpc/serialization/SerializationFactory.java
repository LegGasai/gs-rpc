package com.leggasai.rpc.serialization;

import com.leggasai.rpc.serialization.fst.FstSerialization;
import com.leggasai.rpc.serialization.hessian.HessianSerialization;
import com.leggasai.rpc.serialization.kryo.KryoSerialization;
import com.leggasai.rpc.serialization.nativejdk.NativeJDKSerialization;
import com.leggasai.rpc.serialization.protostuff.ProtostuffSerialization;
import io.protostuff.Rpc;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-24-20:02
 * @Description:
 */
public class SerializationFactory {

    private static final RpcSerialization KRYO_SERIALIZATION_INSTANCE = new KryoSerialization();
    private static final RpcSerialization HESSIAN_SERIALIZATION_INSTANCE = new HessianSerialization();
    private static final RpcSerialization JDK_SERIALIZATION_INSTANCE = new NativeJDKSerialization();
    private static final RpcSerialization PROTOSTUFF_SERIALIZATION_INSTANCE = new ProtostuffSerialization();
    private static final RpcSerialization FST_SERIALIZATION_INSTANCE = new FstSerialization();


    public static RpcSerialization getSerialize(SerializationType serializeType){
        switch (serializeType){
            case KRYOSERIALIZE:
                return KRYO_SERIALIZATION_INSTANCE;
            case HESSIANSERIALIZE:
                return HESSIAN_SERIALIZATION_INSTANCE;
            case JDKSERIALIZE:
                return JDK_SERIALIZATION_INSTANCE;
            case PROTOSTUFFSERIALIZE:
                return PROTOSTUFF_SERIALIZATION_INSTANCE;
            case FSTSERIALIZE:
                return FST_SERIALIZATION_INSTANCE;
            default:
                return HESSIAN_SERIALIZATION_INSTANCE;
        }
    }
}

