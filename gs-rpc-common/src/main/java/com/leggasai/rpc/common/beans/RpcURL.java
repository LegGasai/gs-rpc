package com.leggasai.rpc.common.beans;


import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-03-15:01
 * @Description: 自定义的简易URL,表示服务提供者的信息
 * e.g. 127.0.0.1:8899?version=5&timeout=6000
 * e.g. 127.0.0.1:8899
 * e.g. 127.0.0.1
 */

public class RpcURL {

    private String host;
    private Integer port;
    private Map<String,String> parameters = new HashMap<String,String>();

    public RpcURL(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    public RpcURL(){}

    public RpcURL(String url){
        this();
        String[] split = url.split("\\?");
        if (split.length < 2){
            String address = split[0];
            setAddress(address);

        }else if (split.length == 2){
            String address = split[0];
            setAddress(address);
            String query = split[1];
            setQuery(query);
        }else{
            throw new IllegalArgumentException("Invalid URL");
        }
    }


    /**
     * URL不应该没有端口号
     * @param address
     */
    private void setAddress(String address){
        int splitIndex = address.lastIndexOf(":");
        if (splitIndex<0){
            throw new IllegalArgumentException("Invalid Ip Address, maybe there is no port");
        }else{
            this.host = address.substring(0,splitIndex);
            if (splitIndex < address.length() - 1){
                this.port = Integer.valueOf(address.substring(splitIndex+1));
            }
        }
    }

    private void setQuery(String query){
        String[] kvList = query.split("&");
        for (String kv : kvList) {
            String[] split = kv.split("=");
            if (split.length == 2){
                this.parameters.put(split[0],split[1]);
            }else{
                throw new IllegalArgumentException("Invalid query");
            }
        }
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setParameter(String key,String value){
        this.parameters.put(key, value);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void addParameters(Map<String,String> parameters) {
        this.parameters.putAll(parameters);
    }

    public void addParameter(String key,String value){
        this.parameters.put(key,value);
    }

    public String getParameter(String key){
        return this.parameters.get(key);
    }

    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(host);
        if (port != null){
            sb.append(":").append(port);
        }
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getAddress());
        if (parameters.isEmpty()){
            return sb.toString();
        }else{
            sb.append("?");
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                sb.append(String.format("%s=%s&",entry.getKey(),entry.getValue()));
            }
            sb.deleteCharAt(sb.length()-1);
            return sb.toString();
        }
    }
}
