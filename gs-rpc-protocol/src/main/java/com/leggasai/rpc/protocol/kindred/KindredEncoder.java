package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.serialization.SerializationType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:13
 * @Description: Kindred编码器
 */
public class KindredEncoder extends MessageToByteEncoder<Kindred> {
    private SerializationType serialization;

    public KindredEncoder(SerializationType serialization) {
        this.serialization = serialization;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Kindred kindred, ByteBuf byteBuf) throws Exception {
        // obj -> byte[]

    }
}
