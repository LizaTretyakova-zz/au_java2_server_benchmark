import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
}
