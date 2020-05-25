package de.Stuttgart.Sunbrello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.io.IOException;
import java.net.*;

public class EchoClient {
    private DatagramSocket socket;
    private InetAddress address;
    private String ip;

    private byte[] buf;

    public EchoClient(String ip) {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(ip);
            System.out.println("send to IP/Host address: " + address.getHostAddress());
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println("EchoClient constructor");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public String sendEcho(String msg) {
        buf = msg.getBytes();
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, address, 4445);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String received = new String(
                packet.getData(), 0, packet.getLength());
        return received;
    }

    public void close() {
        socket.close();
    }
}