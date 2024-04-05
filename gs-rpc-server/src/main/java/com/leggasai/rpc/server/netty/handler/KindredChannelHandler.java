package com.leggasai.rpc.server.netty.handler;

import com.leggasai.rpc.protocol.kindred.Kindred;
import com.leggasai.rpc.server.netty.AbstractServerChannelHandler;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:17
 * @Description:
 */
public class KindredChannelHandler extends AbstractServerChannelHandler<Kindred> {


    public KindredChannelHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Kindred kindred) throws Exception {
        // todo
        // 提取kindred中的requestBody，执行submit(requestBody)即可
    }

}
