package Clients;

import Metrics.MetricsAggregator;
import Utilities.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.*;

public class TCPClient extends BaseClient {
    private Exception inThreadException;

    @Override
    public List<Integer> sortData(ServerSocket server, int x, int d, List<Integer> data, MetricsAggregator ma)
            throws IOException, ExecutionException, InterruptedException {
        return Utils.tryConnectWithResourcesAndDoJob(server, (input, output) -> {
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

    public List<Integer> sortData(ServerSocketChannel serverChannel, int x, int d, List<Integer> data, MetricsAggregator ma)
            throws InterruptedException, ExecutionException, IOException {
        ServerSocket server = serverChannel.socket();
        return sortData(server, x, d, data, ma);
    }
}
