package com.leggasai.rpc.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-19-21:48
 * @Description: Zookeeper客户端
 */
public class CuratorClient {
    private Logger logger = LoggerFactory.getLogger(CuratorClient.class);
    private final CuratorFramework client;

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static String ZK_NAMESPACE = "gs-rpc";
    private static int ZK_SESSION_TIMEOUT_MS = 5 * 1000;
    private static int ZK_CONNECTION_TIMEOUT_MS = 60 * 1000;
    public CuratorClient(String ip,int port){
        try {
            String url = ip + ":" + port;
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url)
                    .namespace(ZK_NAMESPACE)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .connectionTimeoutMs(ZK_CONNECTION_TIMEOUT_MS)
                    .sessionTimeoutMs(ZK_SESSION_TIMEOUT_MS);
            client = builder.build();
            client.getConnectionStateListenable().addListener(new ZkConnectionStateListener(ZK_CONNECTION_TIMEOUT_MS,ZK_SESSION_TIMEOUT_MS));
            client.start();
            logger.info("Curator client正在启动，连接Zookeeper：{}",url);
            boolean connected = client.blockUntilConnected(ZK_CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (connected){
                logger.info("Curator client启动成功，连接Zookeeper：{}",url);
            }else{
                logger.error("Curator client连接Zookeeper失败,url:{}",url);
            }
        }catch (Exception e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 检查节点是否存在
     * @param path
     * @return
     */
    public boolean checkExits(String path){
        try {
            if (client.checkExists().forPath(path) != null){
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    /**
     * 判断是否已连接
     * @return
     */
    public boolean isConnected(){
        return client.getZookeeperClient().isConnected();
    }

    /**
     * 关闭连接
     * 用于服务提供者下线时，关闭连接
     */
    public void close(){
        client.close();
    }






    /**
     * Zookeeper 连接状态监听器
     */
    private class ZkConnectionStateListener implements ConnectionStateListener {
        private final long UNKOWN_SESSION_ID = -1L;
        private Logger logger = LoggerFactory.getLogger(ZkConnectionStateListener.class);
        private int timeout;
        private int seesionTimeout;
        private long sessionId;

        public ZkConnectionStateListener(int timeout, int seesionTimeout) {
            this.timeout = timeout;
            this.seesionTimeout = seesionTimeout;
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState state) {
            long sessionId = UNKOWN_SESSION_ID;
            try {
                client.getZookeeperClient().getZooKeeper().getSessionId();
            } catch (Exception e) {
                logger.warn("Curator client状态改变，获取SessionId异常：{}",e.getMessage());
            }

            switch (state){
                case CONNECTED:
                    this.sessionId = sessionId;
                    logger.info("Curator client连接成功，SessionId：{}",sessionId);
                    break;
                case RECONNECTED:
                    if (sessionId == this.sessionId && sessionId != UNKOWN_SESSION_ID){
                        logger.warn("Curator client恢复连接成功，SessionId：{}",sessionId);
                    }else{
                        logger.warn("Curator client重新连接成功，新SessionId：{}，旧SessionId：{}已过期！",sessionId,this.sessionId);
                        this.sessionId = sessionId;
                    }
                    break;
                case LOST:
                    logger.warn("Curator client连接丢失，SessionId：{}",sessionId);
                    break;
                case SUSPENDED:
                    logger.warn("Curator client连接超时，SessionId：{}，Timeout：{}ms",sessionId,timeout);
                    break;
                case READ_ONLY:
                    logger.warn("Curator client进入只读模式，SessionId：{}",sessionId);
                    break;
            }
        }
    }
}
