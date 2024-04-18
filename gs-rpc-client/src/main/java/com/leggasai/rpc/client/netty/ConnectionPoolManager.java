package com.leggasai.rpc.client.netty;


import com.leggasai.rpc.client.invoke.Invoker;
import com.leggasai.rpc.client.netty.handler.AbstractClientChannelHandler;
import com.leggasai.rpc.threadpool.CachedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-09-22:13
 * @Description: netty连接管理池
 * todo LRU淘汰
 */

@Component
public class ConnectionPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolManager.class);

    private static final Long CONNECTION_TIMEOUT = 5000L;
    /**
     * 线程池
     */
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) CachedThreadPool.getExecutor("connection-pool-manager",0,16,60 * 1000,0);

    private ConcurrentHashMap<Invoker,AbstractClientChannelHandler> invoker2Handler = new ConcurrentHashMap<Invoker, AbstractClientChannelHandler>();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();

    private NettyClient nettyClient;

    public void start(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        logger.info("ConnectionPoolManager has startup successfully");
    }

    /**
     * 获取invoker的channelHandler
     * @param invoker
     * @return
     */
    public AbstractClientChannelHandler getHandler(Invoker invoker){
        AbstractClientChannelHandler channelHandler = invoker2Handler.get(invoker);
        if (channelHandler == null){
            askForHandler(invoker);
            waitForChannel();
            channelHandler = invoker2Handler.get(invoker);
            if (channelHandler == null){
                logger.warn("ConnectionPoolManager cannot connect to invoker {}:{} after {} seconds",invoker.getHost(),invoker.getPort(),CONNECTION_TIMEOUT / 1000);
            }else{
                return channelHandler;
            }
        }
        return channelHandler;
    }

    /**
     * 添加连接至invoker的新channel
     * @param invoker
     */
    private void askForHandler(Invoker invoker){
        executor.submit(()->{
            try {
                AbstractClientChannelHandler channelHandler = nettyClient.connect(invoker);
                invoker2Handler.put(invoker,channelHandler);
                signalChannel();
                logger.info("ConnectionPoolManager create a connection to invoker {}:{}",invoker.getHost(),invoker.getPort());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean waitForChannel(){
        lock.lock();
        try {
            logger.warn("ConnectionPoolManager is waiting for current connection to be built.");
            return cond.await(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

    private void signalChannel(){
        lock.lock();
        try {
            cond.signalAll();
        }finally {
            lock.unlock();
        }
    }



    /**
     * 移除并关闭连接至invoker的channel
     * @param invoker
     */
    public void removeAndCloseHandler(Invoker invoker){
        AbstractClientChannelHandler channelHandler = invoker2Handler.get(invoker);
        if (channelHandler!= null) {
            channelHandler.closeChannel();
            invoker2Handler.remove(invoker);
            logger.info("ConnectionPoolManager close a connection to invoker {}:{}",invoker.getHost(),invoker.getPort());
        }
    }


    /**
     * 关闭连接池
     */
    public void close(){
        logger.info("ConnectionPoolManager is shutting down now and waiting for releasing all channels...");
        for (AbstractClientChannelHandler channelHandler : invoker2Handler.values()) {
            channelHandler.closeChannel();
        }
        logger.info("ConnectionPoolManager has shutdown successfully");
    }


}
