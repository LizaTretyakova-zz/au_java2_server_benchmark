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
    public List<Integer> sortData(InetAddress addr, int port, List<Integer> data, MetricsAggregator ma)
            throws IOException, ExecutionException, InterruptedException {
        long start = System.currentTimeMillis();
        return Utils.tryConnectWithResourcesAndDoJob(addr, port, (input, output) -> {
            try {
                Utils.outputMessage(output, data);
                List<Integer> response = Utils.getMessage(input).getArrayList();
                ma.submitAvg(System.currentTimeMillis() - start);
                return response;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

//    public List<Integer> sortData(ServerSocketChannel serverChannel, List<Integer> data, MetricsAggregator ma)
//            throws InterruptedException, ExecutionException, IOException {
//        ServerSocket server = serverChannel.socket();
//        return sortData(server, data, ma);
//    }
}
