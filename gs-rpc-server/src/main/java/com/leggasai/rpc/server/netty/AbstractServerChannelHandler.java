package com.leggasai.rpc.server.netty;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.protocol.heartbeat.HeartBeat;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-22:04
 * @Description: ChannelHandler抽象类
 */
public abstract class AbstractServerChannelHandler<T> extends SimpleChannelInboundHandler<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServerChannelHandler.class);
    protected final TaskManager taskManager;

    protected AbstractServerChannelHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }


    protected CompletableFuture<RpcResponseBody> submitTask(RpcRequestBody request){
        CompletableFuture<RpcResponseBody> future = taskManager.submit(request);
        return future;
    }


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
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();
            logger.warn("Channel {} is idle for {} seconds, and close this channel",ctx.channel(), HeartBeat.HEARTBEAT_TIMEOUT/1000);
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
}
