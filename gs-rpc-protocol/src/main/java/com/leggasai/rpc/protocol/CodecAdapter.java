package com.leggasai.rpc.protocol;

import com.leggasai.rpc.protocol.kindred.KindredCodec;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-05-13:09
 * @Description:
 */
public class CodecAdapter {
    public static Codec getCodec(ProtocolType protocol){
        switch (protocol){
            case KINDRED:
                return new KindredCodec();
            default:
                return null;
        }
    }
}
