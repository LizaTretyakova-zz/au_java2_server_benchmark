import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class BaseClient {
    protected IOException inThreadException = null;

    public abstract List<Integer> sortData(ServerSocket server, List<Integer> data) throws IOException, ExecutionException, InterruptedException;
}
