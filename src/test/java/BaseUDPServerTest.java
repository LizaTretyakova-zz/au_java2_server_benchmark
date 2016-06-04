import Clients.UDPClient;
import Metrics.MetricsAggregator;
import Metrics.Parameter;
import Servers.BaseUDPServer;
import Servers.MultithreadUDPServer;
import Servers.ThreadpoolUDPServer;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class BaseUDPServerTest {
    private final List<Integer> unsorted = Arrays.asList(3, 2, 4, 1, 5);
    private final List<Integer> sorted = Arrays.asList(1, 2, 3, 4, 5);
    private final MetricsAggregator ma = new MetricsAggregator(
            "test", 10, new Parameter("n", 1, 10, 1), new Parameter("m", 10, 10, 0), new Parameter("d", 10, 10, 0)
    );

    public void baseTest(BaseUDPServer server, UDPClient client, List<Integer> data, List<Integer> expected)
            throws IOException, ExecutionException, InterruptedException {
        server.start(ma);
        List<Integer> result = client.sortData(server.getAddr(), server.getPort(), data, ma);
        server.stop();
        assertEquals(result, expected);
    }

    @Test
    public void testMultithreadUDPServer() throws InterruptedException, ExecutionException, IOException {
        MultithreadUDPServer server = new MultithreadUDPServer();
        UDPClient client = new UDPClient();
        baseTest(server, client, unsorted, sorted);
    }

    @Test
    public void testThreadpoolUDPServer() throws InterruptedException, ExecutionException, IOException {
        ThreadpoolUDPServer server = new ThreadpoolUDPServer();
        UDPClient client = new UDPClient();
        baseTest(server, client, unsorted, sorted);
    }
}