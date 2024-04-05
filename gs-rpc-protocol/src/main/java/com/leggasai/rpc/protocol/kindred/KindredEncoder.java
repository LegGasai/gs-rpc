package com.leggasai.rpc.protocol.kindred;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:13
 * @Description:
 */
public class KindredEncoder extends MessageToByteEncoder<Kindred> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Kindred kindred, ByteBuf byteBuf) throws Exception {

    }
}
