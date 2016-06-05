package Servers;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ThreadpoolUDPServer extends BaseUDPServer {

    private ExecutorService threadPool = Executors.newCachedThreadPool();

    @Override
    protected void processClient() {
        while(true) {
            try {
                byte[] input = new byte[PACKET_SIZE];
                ByteArrayOutputStream output = new ByteArrayOutputStream(PACKET_SIZE);
                DatagramPacket packet = new DatagramPacket(input, PACKET_SIZE);
                try {
                    server.receive(packet);
                } catch (SocketException ignored) {
                    LOGGER.info("Got SocketException during accept, assuming socket is closed");
                    return;
                }
                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                threadPool.submit( new Thread(() -> {
                    processClientCore(
                            new DataInputStream(new ByteArrayInputStream(input)),
                            new DataOutputStream(output)
                    );
                    DatagramPacket clientPacket = new DatagramPacket(
                            output.toByteArray(), output.toByteArray().length, address, port);
                    sendPacket(clientPacket);
                }));
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
    public void stop() throws IOException, InterruptedException {
        server.close();
        if(workThreadException != null) {
            throw new RuntimeException(workThreadException);
        }
        threadPool.shutdown();
//        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        workThread.interrupt();
        workThread.join();
        workThread = null;
        ma = null;
    }

    @Override
    public int getPort() {
        return 8086;
    }

    public static int getMAPort() {
        return 8086 + BaseServer.ADDING;
    }
}
