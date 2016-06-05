package Servers;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;


public class MultithreadUDPServer extends BaseUDPServer {

    @Override
    protected void processClient() {
        while(true) {
            try {
                //buffer for reading new packets
                byte[] input = new byte[PACKET_SIZE];
                //output stream
                ByteArrayOutputStream output = new ByteArrayOutputStream(PACKET_SIZE);
                //packet for accepting [no addresses -- we do not send anything]
                DatagramPacket packet = new DatagramPacket(input, PACKET_SIZE);
                //receive data [blocking method]
                try {
                    server.receive(packet);
                } catch (SocketException ignored) {
                    LOGGER.info("Got SocketException during accept, assuming socket is closed");
                    return;
                }
                //extract info from packet -- where to return
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                new Thread(() -> {
                    processClientCore(
                            new DataInputStream(new ByteArrayInputStream(input)),
                            new DataOutputStream(output)
                    );
                    DatagramPacket clientPacket = new DatagramPacket(output.toByteArray(), output.size(), address, port);
                    sendPacket(clientPacket);
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
                if(workThreadException == null) {
                    workThreadException = e;
                }
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getPort() {
        return 8082;
    }

    public static int getMAPort() {
        return 8082 + BaseServer.ADDING;
    }
}
