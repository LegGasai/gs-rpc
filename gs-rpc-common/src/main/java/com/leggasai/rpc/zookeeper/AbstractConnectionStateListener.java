package com.leggasai.rpc.zookeeper;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-21:43
 * @Description:
 */

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper 连接状态监听器
 */
public abstract class AbstractConnectionStateListener implements ConnectionStateListener {
    private final long UNKOWN_SESSION_ID = -1L;
    private final Logger logger = LoggerFactory.getLogger(AbstractConnectionStateListener.class);
    private final int timeout;
    private final int sessionTimeout;
    private long lastSessionId;

    public AbstractConnectionStateListener(int timeout, int sessionTimeout) {
        this.timeout = timeout;
        this.sessionTimeout = sessionTimeout;
    }


    @Override
    public void stateChanged(CuratorFramework client, ConnectionState state) {
        long sessionId = UNKOWN_SESSION_ID;
        try {
            sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
        } catch (Exception e) {
            logger.warn("Curator client状态改变，获取SessionId异常：{}",e.getMessage());
        }

        switch (state){
            case CONNECTED:
                this.lastSessionId = sessionId;
                logger.info("Curator client连接成功，SessionId：{}",Long.toHexString(sessionId));
                doConnect();
                break;
            case RECONNECTED:
                if (sessionId == this.lastSessionId && sessionId != UNKOWN_SESSION_ID){
                    logger.warn("Curator client恢复连接成功，SessionId：{}",Long.toHexString(sessionId));
                    doReconnect(false);
                }else{
                    logger.warn("Curator client重新连接成功，新SessionId：{}，旧SessionId：{} 已过期！",Long.toHexString(sessionId),Long.toHexString(this.lastSessionId));
                    this.lastSessionId = sessionId;
                    doReconnect(true);
                }
                break;
            case LOST:
                logger.warn("Curator client连接丢失，SessionId：{}",Long.toHexString(sessionId));
                doLost();
                break;
            case SUSPENDED:
                logger.warn("Curator client连接超时，SessionId：{}，Timeout：{}ms",Long.toHexString(sessionId),timeout);
                doSuspend();
                break;
            case READ_ONLY:
                logger.warn("Curator client进入只读模式，SessionId：{}",Long.toHexString(sessionId));
                doReadOnly();
                break;
        }
    }

    protected abstract void doReconnect(boolean isExpired);
    protected abstract void doConnect();
    protected abstract void doLost();
    protected abstract void doSuspend();
    protected abstract void doReadOnly();

}
