package com.leggasai.rpc.server.netty;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-22:04
 * @Description:
 */
public abstract class AbstractServerChannelHandler<T> extends SimpleChannelInboundHandler<T> {
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

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
