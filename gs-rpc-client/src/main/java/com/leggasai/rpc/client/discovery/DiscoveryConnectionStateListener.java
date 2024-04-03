package com.leggasai.rpc.client.discovery;

import com.leggasai.rpc.zookeeper.AbstractConnectionStateListener;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-11:03
 * @Description:
 */
public class DiscoveryConnectionStateListener extends AbstractConnectionStateListener {
    private DiscoveryCenter discoveryCenter;
    public DiscoveryConnectionStateListener(DiscoveryCenter discoveryCenter,int timeout, int sesionTimeout){
        super(timeout, sesionTimeout);
        this.discoveryCenter = discoveryCenter;
    }
    @Override
    protected void doReconnect(boolean isExpired) {
        this.discoveryCenter.reSubscribe();
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
