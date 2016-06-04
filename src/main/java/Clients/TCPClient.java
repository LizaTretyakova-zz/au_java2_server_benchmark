package Clients;

import Metrics.MetricsAggregator;
import Utilities.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.*;

public class TCPClient extends BaseClient {
    private Exception inThreadException;

    @Override
    public List<Integer> sortData(InetAddress addr, int port, int x, int d, List<Integer> data, MetricsAggregator ma)
            throws IOException, ExecutionException, InterruptedException {
        return Utils.tryConnectWithResourcesAndDoJob(addr, port, (input, output) -> {
            List<Integer> response = null;
            for(int i = 0; i < x; i++) {
                try {
                    long start = System.currentTimeMillis();
                    Utils.outputMessage(output, data);
                    response = Utils.getMessage(input).getArrayList();
                    ma.submitAvg(System.currentTimeMillis() - start);
                    Thread.sleep(d);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            return response;
        });
    }
}
