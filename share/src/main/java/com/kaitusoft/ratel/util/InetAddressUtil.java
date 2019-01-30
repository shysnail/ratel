package com.kaitusoft.ratel.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class InetAddressUtil {

    public static String getDefaultAddressNotLoopback() throws Exception {
        Enumeration<NetworkInterface> nets;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw e;
        }

        NetworkInterface netinf;
        List<InetAddress> usableInetAdresses = new ArrayList<>();
        while (nets.hasMoreElements()) {
            netinf = nets.nextElement();

            Enumeration<InetAddress> addresses = netinf.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isAnyLocalAddress() && !address.isMulticastAddress()
                        && !(address instanceof Inet6Address) &&!address.isLoopbackAddress()) {
                    String mac = getMac(address);
                    if(!StringUtils.isEmpty(mac)) {
                        usableInetAdresses.add(address);
                    }
                }
            }
        }

//        if(usableInetAdresses.size() > 1) {
//            throw new IllegalStateException("don't know which InetAddress to use, there are more than one: " + usableInetAdresses);
//        } else if(usableInetAdresses.size() == 1) {
//            return usableInetAdresses.get(0).getHostAddress();
//        }
        if(usableInetAdresses.size() > 0){ //如果有多网卡，随便选一个
            return usableInetAdresses.get(0).getHostAddress();
        }

        return null;
    }

    private static String getMac(InetAddress address) throws SocketException {
        NetworkInterface ni=NetworkInterface.getByInetAddress(address);
        if(ni.getName().toUpperCase().startsWith("PPP")){
            //某些vpn网络特征
        }
        byte[] mac= ni.getHardwareAddress();
        if(mac == null || mac.length == 0)
            return null;
        StringBuffer sb = new StringBuffer("");
        for(int i=0; i<mac.length; i++) {
            if(i!=0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i]&0xff;
            String str = Integer.toHexString(temp);
            if(str.length()==1) {
                sb.append("0"+str);
            }else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    public static String getLoopbackAddress(){
        InetAddress loopbackAddress = InetAddress.getLoopbackAddress();
        return loopbackAddress.getHostAddress();
    }


    public static void main(String[] args){
        try {
            getDefaultAddressNotLoopback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}