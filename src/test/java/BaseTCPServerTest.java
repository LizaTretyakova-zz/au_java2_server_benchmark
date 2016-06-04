import Clients.TCPClient;
import Metrics.MetricsAggregator;
import Metrics.Parameter;
import Servers.*;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class BaseTCPServerTest {
    private final List<Integer> unsorted = Arrays.asList(3, 2, 4, 1, 5);
    private final List<Integer> sorted = Arrays.asList(1, 2, 3, 4, 5);
    private final MetricsAggregator ma = new MetricsAggregator(
            "test", 10, new Parameter("n", 1, 10, 1), new Parameter("m", 10, 10, 0), new Parameter("d", 10, 10, 0)
    );
    private final int x = 3;

    public void baseTest(BaseTCPServer server, TCPClient client, List<Integer> data, List<Integer> expected)
            throws IOException, ExecutionException, InterruptedException {
        server.start(ma);
        List<Integer> result = client.sortData(InetAddress.getByName("localhost"), BaseServer.PORT, x, 10, data, ma);
        server.stop();
        assertEquals(result, expected);
    }

    @Test
    public void testSingleThread() throws InterruptedException, ExecutionException, IOException {
        SingleThreadServer server = new SingleThreadServer();
        TCPClient client = new TCPClient();
        baseTest(server, client, unsorted, sorted);
    }

    @Test
    public void testMultithread() throws InterruptedException, ExecutionException, IOException {
        MultithreadServer server = new MultithreadServer();
        TCPClient client = new TCPClient();
        baseTest(server, client, unsorted, sorted);
    }

    @Test
    public void testThreadpool() throws InterruptedException, ExecutionException, IOException {
        ThreadpoolServer server = new ThreadpoolServer();
        TCPClient client = new TCPClient();
        baseTest(server, client, unsorted, sorted);
    }

    @Test
    public void testNonblocking() throws InterruptedException, ExecutionException, IOException {
        NonblockingServer server = new NonblockingServer();
        TCPClient client = new TCPClient();

        server.start(ma);
        Thread.sleep(1000);
        List<Integer> result = client.sortData(InetAddress.getByName("localhost"), BaseServer.PORT, x, 10, unsorted, ma);
        server.stop();
        assertEquals(sorted, result);
    }
}