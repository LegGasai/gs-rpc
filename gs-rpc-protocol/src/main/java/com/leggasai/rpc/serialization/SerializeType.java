package com.leggasai.rpc.serialization;


public enum SerializeType {
    JDKSERIALIZE("jdk"),
    KRYOSERIALIZE("kryo"),
    HESSIANSERIALIZE("hessian"),
    PROTOSTUFFSERIALIZE("protostuff"),
    FSTSERIALIZE("fst");
    private String serializeProtocol;

    private SerializeType(String serializeProtocol) {
        this.serializeProtocol = serializeProtocol;
    }

    public String getSerializeProtocol() {
        return serializeProtocol;
    }

    public static SerializeType getByProtocol(String protocol){
        switch (protocol){
            case "jdk":
                return JDKSERIALIZE;
            case "kryo":
                return KRYOSERIALIZE;
            case "hessian":
                return HESSIANSERIALIZE;
            case "protostuff":
                return PROTOSTUFFSERIALIZE;
            case "fst":
                return FSTSERIALIZE;
            default:
                throw new RuntimeException("不支持的序列化协议");
        }
    }

}

