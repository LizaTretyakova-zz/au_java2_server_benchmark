package Clients;

import Utilities.Utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import java.util.concurrent.*;

public class TCPClient extends BaseClient {
    private Exception inThreadException;

    public List<Integer> sortData(ServerSocket server, List<Integer> data)
            throws IOException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<Integer>> callable = () -> Utils.tryConnectWithResourcesAndDoJob(server, (input, output) -> {
            try {
                Utils.outputMessage(output, data);
                return Utils.getMessage(input).getArrayList();
            } catch (IOException e) {
                e.printStackTrace();
                if(inThreadException == null) {
                    inThreadException = e;
                }
                throw new RuntimeException(e);
            }
        });
        Future<List<Integer>> future = executor.submit(callable);
        List<Integer> result = future.get();
        executor.shutdown();
        return result;
    }

    public List<Integer> sortData(ServerSocketChannel serverChannel, List<Integer> data)
            throws InterruptedException, ExecutionException, IOException {
        ServerSocket server = serverChannel.socket();
        return sortData(server, data);
    }
}
