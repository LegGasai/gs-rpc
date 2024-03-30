package com.leggasai.rpc.codec;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-03-28-20:17
 * @Description:
 */
public class RpcRequestBody implements Serializable {

    private String service;
    private String method;
    private String version;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
    private Map<String,Object> extendField;


    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Map<String, Object> getExtendField() {
        return extendField;
    }

    public void setExtendField(Map<String, Object> extendField) {
        this.extendField = extendField;
    }

    @Override
    public String toString() {
        return "RpcRequestBody{" +
                "service='" + service + '\'' +
                ", method='" + method + '\'' +
                ", version='" + version + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", parameters=" + Arrays.toString(parameters) +
                ", extendField=" + extendField +
                '}';
    }
}
