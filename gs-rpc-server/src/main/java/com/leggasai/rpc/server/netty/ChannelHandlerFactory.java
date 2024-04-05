package com.leggasai.rpc.server.netty;

import com.leggasai.rpc.protocol.ProtocolType;
import com.leggasai.rpc.server.netty.handler.KindredChannelHandler;
import com.leggasai.rpc.server.service.TaskManager;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-22:13
 * @Description:
 */
public class ChannelHandlerFactory {

    public static AbstractServerChannelHandler createChannelHandler(TaskManager taskManager, ProtocolType protocolType){
        switch (protocolType) {
            case KINDRED:
                return new KindredChannelHandler(taskManager);
            default:
                return null;
        }
    }
}
