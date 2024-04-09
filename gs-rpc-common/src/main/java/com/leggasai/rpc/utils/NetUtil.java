package com.leggasai.rpc.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:40
 * @Description: 网络工具类
 */
public final class NetUtil {

    private static volatile String HOST;

    private static volatile String HOST_NAME;

    public static String getLocalHost(){
        if (HOST != null){
            return HOST;
        }
        if (getLocalAddress() == null) {
            return null;
        }else{
            return HOST = getLocalAddress().getHostAddress();
        }

    }

    public static InetAddress getLocalAddress(){
        try {
            InetAddress candidateAddress = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            return inetAddr;
                        }
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }
                    }
                }
            }
            return candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocalHostName(){
        if (HOST_NAME != null) {
            return HOST_NAME;
        }
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            HOST_NAME = Optional.ofNullable(getLocalAddress())
                    .map(k -> k.getHostName())
                    .orElse(null);
        }
        return HOST_NAME;
    }
}
