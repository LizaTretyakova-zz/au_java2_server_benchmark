package Servers;

import Metrics.MetricsAggregator;
import Metrics.ServerMetricsAggregator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public abstract class BaseUDPServer extends BaseServer {

    protected DatagramSocket server;
    protected static final Logger LOGGER = Logger.getLogger("UDPServer");
    protected Thread workThread;

    @Override
    public void start(MetricsAggregator metricsAggregator) throws IOException {
        server = new DatagramSocket(getPort());
        ma = metricsAggregator == null ? new ServerMetricsAggregator(getPort() + ADDING) : metricsAggregator;
        workThread = createWorkThread();
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
        workThread = null;
        ma = null;
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

    public InetAddress getAddr() {
        return server.getLocalAddress();
    }

    public int getPort() {
        return server.getLocalPort();
    }
}
