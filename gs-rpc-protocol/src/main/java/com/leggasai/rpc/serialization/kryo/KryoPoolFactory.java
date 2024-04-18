package com.leggasai.rpc.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializerFactory;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;
import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.util.HashMap;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-22:26
 * @Description:
 */
public class KryoPoolFactory {
    private static volatile KryoPoolFactory poolFactory = null;

    private Pool<Kryo> pool = new Pool(true, false, 8) {
        @Override
        public Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            kryo.register(RpcRequestBody.class);
            kryo.register(RpcResponseBody.class);
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

    private KryoPoolFactory() {
    }

    public static KryoPoolFactory getInstance() {
        if (poolFactory == null) {
            synchronized (KryoPoolFactory.class) {
                if (poolFactory == null) {
                    poolFactory = new KryoPoolFactory();
                }
            }
        }
        return poolFactory;
    }

    public Kryo borrow(){
        return pool.obtain();
    }
    public void release(Kryo kryo) {
        pool.free(kryo);
    }
}
