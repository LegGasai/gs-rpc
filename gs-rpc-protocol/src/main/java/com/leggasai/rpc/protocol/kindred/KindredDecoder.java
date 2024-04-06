package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.serialization.SerializationType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:14
 * @Description: Kindred解码器
 */
public class KindredDecoder extends ByteToMessageDecoder {
    private SerializationType serialization;

    public KindredDecoder(SerializationType serialization) {
        this.serialization = serialization;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // byte[] -> obj

    }
}
