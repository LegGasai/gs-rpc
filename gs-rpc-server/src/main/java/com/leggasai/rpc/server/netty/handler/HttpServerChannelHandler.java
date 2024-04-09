package com.leggasai.rpc.server.netty.handler;

import com.leggasai.rpc.server.netty.AbstractServerChannelHandler;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-22:15
 * @Description:
 */
public class HttpServerChannelHandler extends AbstractServerChannelHandler<FullHttpRequest> {
    public HttpServerChannelHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {

    }
}
