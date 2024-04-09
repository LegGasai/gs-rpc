package com.leggasai.rpc.client.netty.handler;

import com.leggasai.rpc.client.invoke.InvocationManager;
import com.leggasai.rpc.protocol.ProtocolType;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-22:13
 * @Description:
 */
public class ClientChannelHandlerFactory {
    public static AbstractClientChannelHandler createChannelHandler(InvocationManager invocationManager, ProtocolType protocolType){
        switch (protocolType) {
            case KINDRED:
                return new KindredClientChannelHandler(invocationManager);
            default:
                return null;
        }
    }
}
