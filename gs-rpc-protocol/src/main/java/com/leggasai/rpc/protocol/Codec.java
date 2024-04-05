package com.leggasai.rpc.protocol;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:40
 * @Description:
 */


public interface Codec {

    MessageToByteEncoder getEncoder();

    ByteToMessageDecoder getDecoder();

}