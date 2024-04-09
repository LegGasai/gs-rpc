package com.leggasai.rpc.client.netty.handler;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.protocol.kindred.Kindred;
import io.netty.channel.ChannelHandlerContext;

public class KindredClientChannelHandler extends AbstractClientChannelHandler<Kindred>{

    public KindredClientChannelHandler(InvocationManager invocationManager) {
        super(invocationManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Kindred msg) throws Exception {
        invocationManager.markFinish(msg.getRequestId(),msg.getResponseBody());
    }
}
