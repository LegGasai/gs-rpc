package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.protocol.Codec;
import com.leggasai.rpc.serialization.SerializationType;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:12
 * @Description: Kindred codec
 */
public class KindredCodec implements Codec {


    @Override
    public MessageToByteEncoder getEncoder(SerializationType serialization) {

        return new KindredEncoder(serialization);
    }

    @Override
    public ByteToMessageDecoder getDecoder(SerializationType serialization) {
        return new KindredDecoder(serialization);
    }
}
