package com.kaitusoft.ratel;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * @author frog.w
 * @version 1.0.0, 2019/11/13
 *          <p>
 *          write description here
 */
public class UdpServer {

    public static void main(String[] args){
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(58888);
            System.out.println("udp listen 58888");
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf,0,buf.length);
            datagramSocket.receive(dp);
            String receiveData = new String(dp.getData(),0,dp.getLength());
            System.out.println("收到的数据内容："+receiveData);
            datagramSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
