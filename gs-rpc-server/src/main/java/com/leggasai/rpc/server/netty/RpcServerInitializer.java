package com.leggasai.rpc.server.netty;

import com.leggasai.rpc.protocol.Codec;
import com.leggasai.rpc.protocol.CodecAdapter;
import com.leggasai.rpc.protocol.ProtocolType;
import com.leggasai.rpc.server.netty.handler.ChannelHandlerFactory;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-05-13:05
 * @Description:
 */
public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
    private ProtocolType protocol;
    private TaskManager taskManager;
    private Codec codec;

    private static final long CLOSE_TIMEOUT = 3 * 60 * 1000L;

    public RpcServerInitializer(ProtocolType protocol, TaskManager taskManager) {
        this.protocol = protocol;
        this.taskManager = taskManager;
        this.codec = CodecAdapter.getCodec(protocol);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("decoder",codec.getDecoder());
        ch.pipeline().addLast("encoder",codec.getEncoder());
        ch.pipeline().addLast("server-idle-handler",new IdleStateHandler(0,0, CLOSE_TIMEOUT,TimeUnit.MILLISECONDS));
        ch.pipeline().addLast("server-handler", ChannelHandlerFactory.createChannelHandler(taskManager,protocol));
    }
}