package com.leggasai.rpc.client.netty.handler;

import com.leggasai.rpc.client.invoke.Invocation;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.protocol.kindred.Kindred;
import io.netty.channel.ChannelHandlerContext;

public class KindredClientChannelHandler extends AbstractClientChannelHandler<Kindred>{

    public KindredClientChannelHandler(InvocationManager invocationManager) {
        super(invocationManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Kindred msg) throws Exception {
        System.out.println("Receive完成:"+System.nanoTime());
        invocationManager.markFinish(msg.getRequestId(),msg.getResponseBody());
    }

    @Override
    public void invoke(Invocation invocation) {
        Kindred kindred = new Kindred();
        kindred.setRequestId(invocation.getRequestId());
        kindred.setRequest();
        kindred.setNoEvent();
        kindred.setSerialize(invocation.getSerializationType());
        kindred.setRequestBody(invocation.getRequest());
        try {
            long start = System.nanoTime();
            this.channel.writeAndFlush(kindred).sync();
            long end = System.nanoTime();
            System.out.println("Send:"+(end - start));
            System.out.println("Send完成:"+end);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
