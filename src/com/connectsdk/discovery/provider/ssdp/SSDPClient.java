package com.connectsdk.discovery.provider.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

public class SSDPClient {
	DatagramSocket datagramSocket;
    MulticastSocket multicastSocket;
    
    SocketAddress multicastGroup;
    NetworkInterface networkInterface;
    InetAddress localInAddress;

    int timeout = 0;
    static int MX = 5;
    
    public SSDPClient(InetAddress source) throws IOException {
        localInAddress = source;

        multicastSocket = new MulticastSocket(SSDP.PORT);

        multicastGroup = new InetSocketAddress(SSDP.MULTICAST_ADDRESS, SSDP.PORT);
        networkInterface = NetworkInterface.getByInetAddress(localInAddress);
        multicastSocket.joinGroup(multicastGroup, networkInterface);
        
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(localInAddress, 0));
    }

    //Its a package level constructor added just for unit test case in SSDPClientTest just to inject custom socket instances.
    public SSDPClient(InetAddress source, MulticastSocket mcSocket, DatagramSocket dgSocket) throws IOException {
        localInAddress = source;

        multicastSocket = mcSocket;

        multicastGroup = new InetSocketAddress(SSDP.MULTICAST_ADDRESS, SSDP.PORT);
        networkInterface = NetworkInterface.getByInetAddress(localInAddress);
        multicastSocket.joinGroup(multicastGroup, networkInterface);
        
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(localInAddress, 0));
    }
    
    /** Used to send SSDP packet */
    public void send(String data) throws IOException {
        DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), multicastGroup);

        datagramSocket.send(dp);
    }


    /** Used to receive SSDP Response packet */
    public DatagramPacket responseReceive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        datagramSocket.receive(dp);

        return dp;
    }
    
    /** Used to receive SSDP Multicast packet */
    public DatagramPacket multicastReceive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        multicastSocket.receive(dp);

        return dp;
    }
    
//    /** Starts the socket */
//    public void start() {
//    	
//    }

    public boolean isConnected() {
    	return datagramSocket != null && multicastSocket != null && datagramSocket.isConnected() && multicastSocket.isConnected();
    }
    
    /** Close the socket */
    public void close() {
        if (multicastSocket != null) {
            try {
                multicastSocket.leaveGroup(multicastGroup, networkInterface);
            } catch (IOException e) {
                e.printStackTrace();
            }
            multicastSocket.close();
        }
        
        if (datagramSocket != null) {
            datagramSocket.disconnect();
            datagramSocket.close();
        }
    }
    
    public void setTimeout(int timeout) throws SocketException {
    	this.timeout = timeout;
    	datagramSocket.setSoTimeout(this.timeout);
    }
    
    public static String getSSDPSearchMessage(String ST) {
    	StringBuilder sb = new StringBuilder();
    	
        sb.append(SSDP.MSEARCH + SSDP.NEWLINE);
        sb.append("HOST: " + SSDP.MULTICAST_ADDRESS + ":" + SSDP.PORT + SSDP.NEWLINE);
        sb.append("MAN: \"ssdp:discover\"" + SSDP.NEWLINE);
        sb.append("ST: " + ST + SSDP.NEWLINE);
        sb.append("MX: " +  MX + SSDP.NEWLINE);
        if (ST.contains("udap")) {
        	sb.append("USER-AGENT: UDAP/2.0" + SSDP.NEWLINE);
        }
        sb.append(SSDP.NEWLINE);
        
        return sb.toString();    	
    }
}
