package com.leggasai.rpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

/**
 * @Author: Jiang Yichen
 * @Date: 2024-04-02-15:40
 * @Description: 网络工具类
 */
public final class NetUtil {
    private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static final String ANYHOST_VALUE = "0.0.0.0";

    private static final String LOCALHOST_KEY = "localhost";
    private static final String LOCALHOST_VALUE = "127.0.0.1";

    private static volatile InetAddress LOCAL_ADDRESS = null;
    private static volatile Inet6Address LOCAL_ADDRESS_V6 = null;
    private static volatile String HOST_ADDRESS;
    private static volatile String HOST_NAME;


    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    public static String getLocalHostName() {
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
    public static String getLocalHost() {
        if (HOST_ADDRESS != null) {
            return HOST_ADDRESS;
        }

        InetAddress address = getLocalAddress();
        if (address != null) {
            if (address instanceof Inet6Address) {
                String ipv6AddressString = address.getHostAddress();
                if (ipv6AddressString.contains("%")) {
                    ipv6AddressString = ipv6AddressString.substring(0, ipv6AddressString.indexOf("%"));
                }
                HOST_ADDRESS = ipv6AddressString;
                return HOST_ADDRESS;
            }

            HOST_ADDRESS = address.getHostAddress();
            return HOST_ADDRESS;
        }

        return LOCALHOST_VALUE;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            NetworkInterface networkInterface = findNetworkInterface();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                if (addressOp.isPresent()) {
                    try {
                        if (addressOp.get().isReachable(100)) {
                            return addressOp.get();
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        try {
            localAddress = InetAddress.getLocalHost();
            Optional<InetAddress> addressOp = toValidAddress(localAddress);
            if (addressOp.isPresent()) {
                return addressOp.get();
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }

        localAddress = getLocalAddressV6();

        return localAddress;
    }

    public static Inet6Address getLocalAddressV6() {
        if (LOCAL_ADDRESS_V6 != null) {
            return LOCAL_ADDRESS_V6;
        }
        Inet6Address localAddress = getLocalAddress0V6();
        LOCAL_ADDRESS_V6 = localAddress;
        return localAddress;
    }

    private static Inet6Address getLocalAddress0V6() {
        // @since 2.7.6, choose the {@link NetworkInterface} first
        try {
            NetworkInterface networkInterface = findNetworkInterface();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet6Address) {
                    if (!address.isLoopbackAddress() // filter ::1
                            && !address.isAnyLocalAddress() // filter ::/128
                            && !address.isLinkLocalAddress() // filter fe80::/10
                            && !address.isSiteLocalAddress() // filter fec0::/10
                            && !isUniqueLocalAddress(address) // filter fd00::/8
                            && address.getHostAddress().contains(":")) { // filter IPv6
                        return (Inet6Address) address;
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static boolean isUniqueLocalAddress(InetAddress address) {
        byte[] ip = address.getAddress();
        return (ip[0] & 0xff) == 0xfd;
    }

    public static NetworkInterface findNetworkInterface() {

        List<NetworkInterface> validNetworkInterfaces = emptyList();
        try {
            validNetworkInterfaces = getValidNetworkInterfaces();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        NetworkInterface result = null;

        for (NetworkInterface networkInterface : validNetworkInterfaces) {
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                Optional<InetAddress> addressOp = toValidAddress(addresses.nextElement());
                if (addressOp.isPresent()) {
                    try {
                        if (addressOp.get().isReachable(100)) {
                            return networkInterface;
                        }
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }


        if (result == null) {
            result = CollectionUtils.firstElement(validNetworkInterfaces);
        }

        return result;
    }
    private static List<NetworkInterface> getValidNetworkInterfaces() throws SocketException {
        List<NetworkInterface> validNetworkInterfaces = new LinkedList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) { // ignore
                continue;
            }
            validNetworkInterfaces.add(networkInterface);
        }
        return validNetworkInterfaces;
    }


    private static Optional<InetAddress> toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address v6Address = (Inet6Address) address;
            if (isPreferIPV6Address()) {
                return Optional.ofNullable(normalizeV6Address(v6Address));
            }
        }
        if (isValidV4Address(address)) {
            return Optional.of(address);
        }
        return Optional.empty();
    }





    static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
                logger.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    static boolean isValidV4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }

        String name = address.getHostAddress();
        return (name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANYHOST_VALUE.equals(name)
                && !LOCALHOST_VALUE.equals(name));
    }

}