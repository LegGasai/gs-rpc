package com.leggasai.rpc.client.netty;

import com.leggasai.rpc.client.discovery.DiscoveryCenter;
import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.netty.handler.AbstractClientChannelHandler;
import com.leggasai.rpc.config.ConsumerProperties;
import com.leggasai.rpc.protocol.ProtocolType;
import com.leggasai.rpc.serialization.SerializationType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:40
 * @Description: 服务调用者-netty端
 */
@Component
@DependsOn("nettyServer")
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;
    @Autowired
    private ConsumerProperties consumerProperties;

    @Autowired
    private InvocationManager invocationManager;

    @Autowired
    private DiscoveryCenter discoveryCenter;

    @Autowired
    private ConnectionPoolManager connectionPoolManager;

    @PostConstruct
    public void startup(){
        // 开启netty服务器
        initClient();
        // 开启connectionManager
        connectionPoolManager.start(this);
        // 订阅服务
        discoveryCenter.subscribeService();
    }

    private void initClient(){
        logger.info("NettyClient is starting...");
        ProtocolType protocol = ProtocolType.getByName(consumerProperties.getProtocol());
        SerializationType serialization = SerializationType.getByProtocol(consumerProperties.getSerialization());
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new RpcClientInitializer(protocol,serialization,invocationManager));
        logger.info("NettyClient init success.");
    }

    /**
     * 建立长连接
     * @param invoker
     * @return
     */
    public AbstractClientChannelHandler connect(Invoker invoker) throws InterruptedException{
        // 返回handler
        String host = invoker.getHost();
        Integer port = invoker.getPort();
        ChannelFuture future = bootstrap.connect(host, port);
        future.sync();
        logger.info("NettyClient connects to {}:{} successfully.", host, port);
        return future.channel().pipeline().get(AbstractClientChannelHandler.class);
    }

    @PreDestroy
    public void shutdown(){
        // 关闭DiscoveryCenter
        discoveryCenter.close();
        // 关闭调用中心
        invocationManager.shutdown();
        // 关闭连接池
        connectionPoolManager.close();
        // 关闭netty
        eventLoopGroup.shutdownGracefully();
        logger.info("NettyClient has shutdown successfully");
    }
}
