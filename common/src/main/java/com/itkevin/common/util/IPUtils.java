package com.itkevin.common.util;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class IPUtils {
    private static String hostName;

    /**
     * 获取本机ip
     * @return
     */
    public static String getLocalIp() {
        try {
            //一个主机有多个网络接口
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = netInterfaces.nextElement();
                //每个网络接口,都会有多个"网络地址",比如一定会有loopback地址,会有siteLocal地址等.以及IPV4或者IPV6    .
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    //get only :172.*,192.*,10.*
                    if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 获取本机hostname
     * @return
     */
    public static String getLocalHostName() {
        if (StringUtils.isNotBlank(hostName)) {
            return hostName;
        }
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            String host = uhe.getMessage();
            if (host != null) {
                int colon = host.indexOf(':');
                if (colon > 0) {
                    return host.substring(0, colon);
                }
            }
        }
        if (StringUtils.isNotBlank(hostname) && hostname.contains("-")) {
            hostName = hostname;
            return hostname;
        }
        InputStream is = null;
        try {
            Process p = Runtime.getRuntime().exec("hostname");
            byte[] hostBytes = new byte[1024];
            is = p.getInputStream();
            int readed = is.read(hostBytes);
            p.waitFor();
            hostName = new String(hostBytes, 0, readed);
            if (StringUtils.isNotBlank(hostName)) {
                hostName = hostName.trim();
            }
            return hostName;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        hostName = "unknownHostname";
        return hostName;
    }

}
