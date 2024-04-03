package com.leggasai.rpc.server.registry;

import com.leggasai.rpc.zookeeper.AbstractConnectionStateListener;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-21:49
 * @Description:
 */
public class RegistryConnectionStateListener extends AbstractConnectionStateListener {
    private RegistryCenter registryCenter;
    public RegistryConnectionStateListener(RegistryCenter registryCenter,int timeout, int sesionTimeout){
        super(timeout, sesionTimeout);
        this.registryCenter = registryCenter;
    }

    /**
     * if reconnect with the registry center because of networking when the session has been expired, it will try to register all services.
     */
    @Override
    protected void doReconnect(boolean isExpired) {
        if (isExpired){
            registryCenter.reRegisterAllServices();
        }
    }

    @Override
    protected void doConnect() {

    }

    @Override
    protected void doLost() {

    }

    @Override
    protected void doSuspend() {

    }

    @Override
    protected void doReadOnly() {

    }
}
