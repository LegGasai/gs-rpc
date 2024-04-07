package com.leggasai.rpc.protocol.kindred;

import com.leggasai.rpc.codec.RpcRequestBody;
import com.leggasai.rpc.codec.RpcResponseBody;
import com.leggasai.rpc.exception.ErrorCode;
import com.leggasai.rpc.serialization.SerializationType;
import com.leggasai.rpc.utils.Snowflake;

import java.io.Serializable;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-19:12
 * @Description: RPC协议
 */
public class Kindred implements Serializable {

    public static final int HEADER_LENGTH = 16;
    private static final short MAGIC_NUMBER = (short)0xCDED;
    private static final byte IS_REQUEST_MASK = (byte)0x80;
    private static final byte NEED_DATA_MASK = (byte)0x40;
    private static final byte IS_EVENT_MASK = (byte)0x20;
    private static final byte SERIALIZE_MASK = (byte)0x1F;

    /**
     * --------------------------------------------------
     * 属性分割线
     */

    /**
     * 魔数Kindred协议固定为0xCDED
     * 16bit(2Byte)
     */
    private Short magic = MAGIC_NUMBER;

    /**
     * bit信息位
     * 8bit(1Byte)
     * Request/Response（1bit）：标识是Request还是Response
     * Need return（1bit）：用于标识该Request是否需要服务端返回数据
     * Event（1bit）：是否是事件，心跳事件
     * Serialize Type（5bit）：序列化方式
     */
    private Byte bitInfo = (byte)0x00;

    /**
     * 请求状态(Response) {@link com.leggasai.rpc.exception.ErrorCode}
     * 8bit(1Byte)
     */
    private Byte status = (byte)0x00;

    /**
     * RPC标识-雪花ID生成全局唯一
     * 64bit(8Byte)
     */
    private Long requestId;

    /**
     * 请求体长度
     * 32bit(4Byte)
     */
    private Integer length;

    /**
     * Request请求体
     * length Byte
     */
    private RpcRequestBody requestBody;

    /**
     * Response响应体
     * length Byte
     */
    private RpcResponseBody responseBody;

    public Kindred() {
        this.requestId = Snowflake.generateId();
    }

    public Kindred(Long requestId) {
        this.requestId = requestId;
    }

    public Short getMagic() {
        return magic;
    }

    public void setStatus(ErrorCode errorCode){
        this.status = (byte)errorCode.getCode();
    }

    public RpcRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RpcRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public RpcResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(RpcResponseBody responseBody) {
        this.responseBody = responseBody;
    }


    public static Boolean isKindred(Short magic){
        return magic != null && magic == MAGIC_NUMBER;
    }

    public Byte getBitInfo() {
        return bitInfo;
    }

    public Byte getStatus() {
        return status;
    }

    public Long getRequestId() {
        return requestId;
    }

    public Integer getLength() {
        return length;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public void setBitInfo(Byte bitInfo) {
        this.bitInfo = bitInfo;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    private byte getIsRequest(){
        return (byte) ((bitInfo & IS_REQUEST_MASK) >> 7);
    }

    private byte getNeedData(){
        return (byte) ((bitInfo & NEED_DATA_MASK) >> 6);
    }

    private byte getIsEvent(){
        return (byte) ((bitInfo & IS_EVENT_MASK) >> 5);
    }

    public byte getSerializeId(){
        return (byte) (bitInfo & SERIALIZE_MASK);
    }

    public Boolean isRequest() {
        return getIsRequest() == 0;
    }

    public Boolean needReturnData(){
        return getNeedData() == 1;
    }

    public Boolean isEvent(){
        return getIsEvent() == 1;
    }

    public void setRequest(){
        this.bitInfo = (byte)(this.bitInfo & (~IS_REQUEST_MASK));
    }

    public void setResponse(){
        this.bitInfo = (byte)(this.bitInfo | IS_REQUEST_MASK);
    }

    public void setNeedData(){
        this.bitInfo = (byte)(this.bitInfo | NEED_DATA_MASK);
    }

    public void setNoData(){
        this.bitInfo = (byte)(this.bitInfo & (~NEED_DATA_MASK));
    }

    public void setEvent(){
        this.bitInfo = (byte)(this.bitInfo | IS_EVENT_MASK);
    }

    public void setNoEvent(){
        this.bitInfo = (byte)(this.bitInfo & (~IS_EVENT_MASK));
    }

    public void setSerialize(SerializationType serialize){
        byte serializeId = serialize.getSerializeId().byteValue();
        this.bitInfo = (byte)((this.bitInfo & ~SERIALIZE_MASK) | serializeId);
    }



}
