package com.leggasai.rpc.serialization;


public enum SerializationType {
    JDKSERIALIZE("jdk",0),
    KRYOSERIALIZE("kryo",1),
    HESSIANSERIALIZE("hessian",2),
    PROTOSTUFFSERIALIZE("protostuff",3),
    FSTSERIALIZE("fst",4);
    private final String serializeProtocol;

    private final Integer serializeId;

    private SerializationType(String serializeProtocol,Integer serializeId) {
        this.serializeProtocol = serializeProtocol;
        this.serializeId = serializeId;
    }

    public String getSerializeProtocol() {
        return serializeProtocol;
    }

    public Integer getSerializeId() {
        return serializeId;
    }

    public static SerializationType getByProtocol(String protocol){
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

    public static SerializationType getBySerializeId(Integer serializeId){
        switch (serializeId){
            case 0:
                return JDKSERIALIZE;
            case 1:
                return KRYOSERIALIZE;
            case 2:
                return HESSIANSERIALIZE;
            case 3:
                return PROTOSTUFFSERIALIZE;
            case 4:
                return FSTSERIALIZE;
            default:
                throw new RuntimeException("不支持的序列化协议");
        }
    }

}

