package com.leggasai.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.enums.ResponseType;
import com.leggasai.rpc.exception.RpcException;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.HashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-22:26
 * @Description: Kryo Pool工厂（单例）
 */
public class KryoPoolFactory {
    /**
     * 对象池解决线程安全问题
     */
    private static Pool<Kryo> pool = new Pool(true, false, 8) {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(true); //解决循环引用问题
            kryo.register(RpcRequestBody.class);
            kryo.register(RpcResponseBody.class);
            kryo.register(Exception.class);
            kryo.register(RpcException.class);
            kryo.register(HashMap.class);
            kryo.register(Object[].class);
            kryo.register(Class[].class);
            kryo.register(String[].class);
            kryo.register(Object.class);
            kryo.register(String.class);

            CompatibleFieldSerializer.CompatibleFieldSerializerConfig config = new CompatibleFieldSerializer.CompatibleFieldSerializerConfig();
            config.setExtendedFieldNames(false);
            config.setReadUnknownFieldData(true);
            kryo.setDefaultSerializer(new SerializerFactory.CompatibleFieldSerializerFactory(config));
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
            return kryo;
        }
    };


    public static Kryo borrow(){
        return pool.obtain();
    }
    public static void release(Kryo kryo) {
        pool.free(kryo);
    }
}
