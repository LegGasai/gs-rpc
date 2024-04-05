package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.protocol.Codec;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:12
 * @Description: Kindred codec
 */
public class KindredCodec implements Codec {



    @Override
    public MessageToByteEncoder getEncoder() {
        return null;
    }

    @Override
    public ByteToMessageDecoder getDecoder() {
        return null;
    }
}
