package com.leggasai.rpc.common.beans;

import com.sun.org.apache.bcel.internal.generic.RETURN;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:51
 * @Description: 服务元信息
 */
public class ServiceMeta {

    private final String serviceName;

    private final String version;

    public ServiceMeta(String serviceName, String version) {
        this.serviceName = serviceName;
        this.version = version;
    }

    public ServiceMeta(String serviceName) {
        this.serviceName = serviceName;
        this.version = "";
    }

    public String getServiceKey(){
        return serviceName + "#" + version;
    }

}
