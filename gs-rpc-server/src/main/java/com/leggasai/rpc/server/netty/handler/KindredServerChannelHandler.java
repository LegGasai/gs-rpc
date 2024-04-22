package com.leggasai.rpc.server.netty.handler;

import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.enums.ResponseType;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.protocol.kindred.Kindred;
import com.leggasai.rpc.server.netty.AbstractServerChannelHandler;
import com.leggasai.rpc.server.service.TaskManager;
import com.leggasai.rpc.utils.TimeUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CompletableFuture;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:17
 * @Description:
 */
public class KindredServerChannelHandler extends AbstractServerChannelHandler<Kindred> {


    public KindredServerChannelHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Kindred kindred) throws Exception {
        long start = TimeUtil.getNanoTime();
        System.out.println("channelRead0 start at:"+start);
        CompletableFuture<RpcResponseBody> future = submitTask(kindred.getRequestBody());
        future.thenAccept((response)->{
            transform(kindred,response);
            long start1 = TimeUtil.getNanoTime();
            System.out.println("server writeAndFlush start:"+start1);
            ChannelFuture channelFuture = channelHandlerContext.writeAndFlush(kindred);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    TimeUtil.printCostTime("server writeAndFlush",start1);
                }
            });
            TimeUtil.printCostTime("server channelRead0",start1);
        });
    }

    private void transform(Kindred kindred,RpcResponseBody response){
        kindred.setResponse();
        kindred.setResponseBody(response);
        ResponseType responseType = response.getResponseType();
        if (responseType == ResponseType.RESPONSE_VALUE){
            kindred.setNeedData();
            kindred.setStatus(ErrorCode.OK);
        }else if (responseType == ResponseType.RESPONSE_NULL){
            kindred.setNoData();
            kindred.setStatus(ErrorCode.OK);
        }else if (responseType == ResponseType.RESPONSE_ERROR){
            RpcException exception = (RpcException) response.getResult();
            kindred.setStatus((byte)exception.getCode());
        }
    }
}
