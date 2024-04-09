package com.leggasai.rpc.client.netty;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.netty.handler.ClientChannelHandlerFactory;
import com.leggasai.rpc.protocol.Codec;
import com.leggasai.rpc.protocol.CodecAdapter;
import com.leggasai.rpc.protocol.ProtocolType;
import com.leggasai.rpc.protocol.heartbeat.HeartBeat;
import com.leggasai.rpc.serialization.SerializationType;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-09-15:05
 * @Description:
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {

    private ProtocolType protocol;
    private SerializationType serialization;
    private InvocationManager invocationManager;
    private Codec codec;

    public RpcClientInitializer(ProtocolType protocol, SerializationType serialization, InvocationManager invocationManager) {
        this.protocol = protocol;
        this.serialization = serialization;
        this.invocationManager = invocationManager;
        this.codec = CodecAdapter.getCodec(protocol);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast("client-idle-handler",new IdleStateHandler(0,0, HeartBeat.HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS));
        ch.pipeline().addLast("decoder",codec.getDecoder(serialization));
        ch.pipeline().addLast("encoder",codec.getEncoder(serialization));
        ch.pipeline().addLast("client-handler", ClientChannelHandlerFactory.createChannelHandler(invocationManager, protocol));
    }
}
