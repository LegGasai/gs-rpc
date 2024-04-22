package com.leggasai.rpc.client.netty.handler;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.protocol.kindred.Kindred;
import com.leggasai.rpc.utils.TimeUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class KindredClientChannelHandler extends AbstractClientChannelHandler<Kindred>{

    public KindredClientChannelHandler(InvocationManager invocationManager) {
        super(invocationManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Kindred msg) throws Exception {
        System.out.println("client channelRead0:"+TimeUtil.getNanoTime());
        invocationManager.markFinish(msg.getRequestId(),msg.getResponseBody());
    }

    @Override
    public void invoke(Invocation invocation) {
        long start = TimeUtil.getNanoTime();
        Kindred kindred = new Kindred();
        kindred.setRequestId(invocation.getRequestId());
        kindred.setRequest();
        kindred.setNoEvent();
        kindred.setSerialize(invocation.getSerializationType());
        kindred.setRequestBody(invocation.getRequest());

        ChannelFuture channelFuture = this.channel.writeAndFlush(kindred);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                TimeUtil.printCostTime("client writeAndFlush",start);
            }
        });
        TimeUtil.printCostTime("invoke",start);

    }
}
