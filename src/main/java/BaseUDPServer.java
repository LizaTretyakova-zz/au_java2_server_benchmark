import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public abstract class BaseUDPServer extends BaseServer {

    protected DatagramSocket server;

    @Override
    public void start() throws IOException {
        server = new DatagramSocket(PORT);
        workThread.start();
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        server.close();
        if(workThreadException != null) {
            throw new RuntimeException(workThreadException);
        }
        workThread.interrupt();
        workThread.join();
    }

    protected void sendPacket(DatagramPacket clientPacket) {
        try {
            server.send(clientPacket);
        } catch (IOException e) {
            e.printStackTrace();
            if(workThreadException == null) {
                workThreadException = e;
            }
            throw new RuntimeException(e);
        }
    }
}
