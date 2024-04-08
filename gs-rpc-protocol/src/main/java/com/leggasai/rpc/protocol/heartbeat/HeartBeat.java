package com.leggasai.rpc.protocol.heartbeat;
/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:12
 * @Description: RPC心跳包
 */
public final class HeartBeat {
    public static final short MAGIC_NUMBER = (short)0xB6B6;
    public static final long HEARTBEAT_INTERVAL = 30 * 1000L;
    public static final long HEARTBEAT_TIMEOUT = HEARTBEAT_INTERVAL * 3;
    public static Boolean isHeatBeat(Short magic){
        return magic != null && magic == MAGIC_NUMBER;
    }

}
