package com.leggasai.rpc.client.netty.handler;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.exception.RpcException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-09-22:13
 * @Description: 客户端ChannelHandler抽象类
 */
public abstract class AbstractClientChannelHandler<T> extends SimpleChannelInboundHandler<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClientChannelHandler.class);
    protected final InvocationManager invocationManager;

    protected Channel channel;
    protected AbstractClientChannelHandler(InvocationManager invocationManager) {
        this.invocationManager = invocationManager;
    }

    public abstract void invoke(Invocation invocation);

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("Client channel {} is registered",ctx.channel());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        this.channel = channel;
        logger.info("The connection of {} <-> {} is established.",channel.remoteAddress(),channel.localAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            // 发送心跳
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof RpcException){
            logger.error("Exception in channel pipeline and will close this channel {}",ctx.channel(),cause);
        }
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    public void closeChannel(){
        if (channel != null && channel.isActive()){
            channel.close().syncUninterruptibly();
            logger.info("The connection of {} <-> {} is closed.",channel.remoteAddress(),channel.localAddress());
        }
    }
}
