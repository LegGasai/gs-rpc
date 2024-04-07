package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.exception.RpcException;
import com.leggasai.rpc.serialization.RpcSerialization;
import com.leggasai.rpc.serialization.SerializationFactory;
import com.leggasai.rpc.serialization.SerializationType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:14
 * @Description: Kindred解码器
 */
public class KindredDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(KindredDecoder.class);
    private SerializationType serialization;

    private RpcSerialization serializer;
    public KindredDecoder(SerializationType serialization) {
        this.serialization = serialization;
        this.serializer = SerializationFactory.getSerialize(this.serialization);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // byte[] -> obj
        if (byteBuf.readableBytes() < Kindred.HEADER_LENGTH) {
            return;
        }
        byteBuf.markReaderIndex();
        try {
            Short magic  = byteBuf.readShort();
            if (!Kindred.isKindred(magic)){
                logger.error("KindredDecoder无法解析非Kindred协议");
                throw new RpcException(ErrorCode.SERVER_ERROR.getCode(),ErrorCode.SERVER_ERROR.getMessage());
            }
            Kindred kindred = new Kindred();
            byte bitInfo = byteBuf.readByte();
            byte status = byteBuf.readByte();
            long requestId = byteBuf.readLong();
            int length = byteBuf.readInt();
            if (byteBuf.readableBytes() < length){
                byteBuf.resetReaderIndex();
                return;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);
            kindred.setBitInfo(bitInfo);
            kindred.setStatus(status);
            kindred.setRequestId(requestId);
            kindred.setLength(length);
            if (kindred.isRequest()){
                kindred.setRequestBody((RpcRequestBody) serializer.deserialize(bytes, RpcRequestBody.class));
            }else{
                kindred.setResponseBody((RpcResponseBody) serializer.deserialize(bytes, RpcResponseBody.class));
            }
            list.add(kindred);
        }catch (Exception e){
            logger.error("KindredDecoder在解析时出现异常",e);
            throw new RpcException(ErrorCode.SERVER_ERROR.getCode(),ErrorCode.SERVER_ERROR.getMessage(),e);
        }
    }
}
