package com.leggasai.rpc.server.netty;

import com.leggasai.rpc.common.beans.RpcURL;
import com.leggasai.rpc.config.ProviderProperties;
import com.leggasai.rpc.protocol.ProtocolType;
import com.leggasai.rpc.serialization.SerializationType;
import com.leggasai.rpc.server.registry.RegistryCenter;
import com.leggasai.rpc.server.service.TaskManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:40
 * @Description: 服务提供者-netty端
 */
@Component
public class NettyServer{
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private RegistryCenter registryCenter;

    @Autowired
    private ProviderProperties providerProperties;

    @Autowired
    private TaskManager taskManager;

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private InetSocketAddress localAddress;

    @PostConstruct
    public void startup(){
        // 开启netty服务器
        initServer();
    }

    private void initServer(){
        logger.info("NettyServer is starting...");
        Integer port = providerProperties.getPort();
        ProtocolType protocol = ProtocolType.getByName(providerProperties.getProtocol());
        SerializationType serialization = SerializationType.getByProtocol(providerProperties.getSerialization());
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        bootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, providerProperties.getAccepts())
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                .childHandler(new RpcServerInitializer(protocol,serialization,taskManager));
        // bind address
        try {
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            localAddress = (InetSocketAddress) channelFuture.channel().localAddress();
            logger.info("NettyServer startup successfully in {}",localAddress);
            // 注册服务
            RpcURL rpcURL = new RpcURL();
            rpcURL.setHost(localAddress.getHostName());
            rpcURL.setPort(localAddress.getPort());
            rpcURL.setParameter("weight",String.valueOf(providerProperties.getWeight()));
            registryCenter.setRpcURL(rpcURL);
            registryCenter.register();
        }catch (InterruptedException e){
            logger.error("NettyServer startup error",e);
        }
    }


    @PreDestroy
    public void shutdown(){
        // 关闭registryCenter 拒绝接收新的服务
        registryCenter.unregister();
        // 延迟关闭TaskManager，等待任务全部执行完毕，但最多延迟<优雅关闭的时间>
        taskManager.shutdown();
        // 关闭netty
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("NettyServer has shutdown successfully");
    }
}
