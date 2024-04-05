package com.leggasai.rpc.protocol.kindred;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:12
 * @Description: RPC协议
 */
public class Kindred {

    private static final int HEADER_LENGTH = 16;
    private static final short MAGIC_NUMBER = (short)0xCDED;
    private static final byte IS_REQUEST_MASK = (byte)0x80;
    private static final byte NEED_DATA_MASK = (byte)0x40;
    private static final byte IS_EVENT_MASK = (byte)0x20;
    private static final byte STATUS_MASK = (byte)0x1F;



}
