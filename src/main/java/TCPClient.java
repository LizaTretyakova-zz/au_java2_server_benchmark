import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.*;

public class TCPClient extends BaseClient {

    @Override
    public List<Integer> sortData(ServerSocket server, List<Integer> data)
            throws IOException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<Integer>> callable = () -> Utils.tryConnectWithResourcesAndDoJob(server, (input, output) -> {
            try {
                Utils.sendMessage(output, data);
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
}
