package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.serialization.RpcSerialization;
import com.leggasai.rpc.serialization.SerializationFactory;
import com.leggasai.rpc.serialization.SerializationType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:13
 * @Description: Kindred编码器
 */
public class KindredEncoder extends MessageToByteEncoder<Kindred> {
    private static final Logger logger = LoggerFactory.getLogger(KindredEncoder.class);
    private SerializationType serialization;
    private RpcSerialization serializer;
    public KindredEncoder(SerializationType serialization) {
        this.serialization = serialization;
        this.serializer = SerializationFactory.getSerialize(this.serialization);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Kindred kindred, ByteBuf byteBuf) throws Exception {
        // obj -> byte[]
        try {
            if (!Kindred.isKindred(kindred.getMagic())){
                logger.error("KindredEncoder无法对非Kindred协议进行编码");
                throw new RpcException(ErrorCode.SERVER_ERROR.getCode(),ErrorCode.SERVER_ERROR.getMessage());
            }
            byteBuf.writeShort(kindred.getMagic());
            byteBuf.writeByte(kindred.getBitInfo());
            byteBuf.writeByte(kindred.getStatus());
            byteBuf.writeLong(kindred.getRequestId());
            byte[] body = null;
            if (kindred.isRequest()){
                body = this.serializer.serialize(kindred.getRequestBody());
            }else{
                body = this.serializer.serialize(kindred.getResponseBody());
            }
            byteBuf.writeInt(body.length);
            byteBuf.writeBytes(body);
        }catch (Exception e){
            logger.error("KindredEncoder在编码时出现异常",e);
            throw new RpcException(ErrorCode.SERVER_ERROR.getCode(),ErrorCode.SERVER_ERROR.getMessage());
        }
    }
}
