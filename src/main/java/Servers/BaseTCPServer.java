package Servers;

import Metrics.BaseMetricsAggregator;
import Metrics.MetricsAggregator;
import Metrics.ServerMetricsAggregator;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class BaseTCPServer extends BaseServer {
//    public static final int PORT = 8081;

    protected ServerSocket server;
    protected IOException workThreadException = null;
    protected Thread workThread;

    @Override
    protected Thread createWorkThread() {
        return new Thread(() -> {
            while (!Thread.interrupted() && !server.isClosed()) {
                processClient();
            }
        });
    }

    @Override
    public void start(MetricsAggregator metricsAggregator) throws IOException {
        server = new ServerSocket(getPort());
        ma = metricsAggregator == null ? new ServerMetricsAggregator(getPort() + ADDING) : metricsAggregator;
        workThread = createWorkThread();
        workThread.start();
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close();
        workThread.interrupt();
        workThread.join();
        workThread = null;
        ma = null;
    }

    @Override
    public ServerSocket getServer() {
        return server;
    }
}
