package com.leggasai.rpc.server.netty.handler;


import com.leggasai.rpc.protocol.heartbeat.HeartBeat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo
// 处理客户端心跳 监测心跳魔数
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        if (in.readableBytes() < 2){
            ctx.fireChannelRead(msg);
            return;
        }
        in.markReaderIndex();
        Short magic = in.readShort();
        if (HeartBeat.isHeatBeat(magic)){
            logger.info("Receive a heartbeat from client {}", ctx.channel().remoteAddress());
        }else{
            in.resetReaderIndex();
            ctx.fireChannelRead(msg);
        }
    }
}
