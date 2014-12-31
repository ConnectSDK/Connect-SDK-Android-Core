/*
 * SSDPSocket
 * Connect SDK
 * 
 * Copyright (c) 2014 LG Electronics.
 * Copyright (c) 2011 stonker.lee@gmail.com https://code.google.com/p/android-dlna/
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.connectsdk.core.upnp.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;

public class SSDPSocket {
    DatagramSocket datagramSocket;
    MulticastSocket multicastSocket;
    
    SocketAddress multicastGroup;
    NetworkInterface networkInterface;
    InetAddress localInAddress;

    int timeout = 0;
    
    public SSDPSocket(InetAddress source) throws IOException {
        localInAddress = source;

        multicastSocket = new MulticastSocket(SSDP.PORT);

        multicastGroup = new InetSocketAddress(SSDP.ADDRESS, SSDP.PORT);
        networkInterface = NetworkInterface.getByInetAddress(localInAddress);
        multicastSocket.joinGroup(multicastGroup, networkInterface);
        
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(localInAddress, 0));
    }

    //Its a package level constructor added just for unit test case in SSDPSocketTest just to inject custom socket instances.
    SSDPSocket(InetAddress source, MulticastSocket mcSocket, DatagramSocket dgSocket ) throws IOException {
        localInAddress = source;

        multicastSocket = mcSocket;

        multicastGroup = new InetSocketAddress(SSDP.ADDRESS, SSDP.PORT);
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
}