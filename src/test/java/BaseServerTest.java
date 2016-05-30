import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class BaseServerTest {
    private final List<Integer> unsorted = Arrays.asList(3, 2, 4, 1, 5);
    private final List<Integer> sorted = Arrays.asList(1, 2, 3, 4, 5);

    public void baseTest(BaseServer server, BaseClient client, List<Integer> data, List<Integer> expected)
            throws IOException, ExecutionException, InterruptedException {
        server.start();
        List<Integer> result = client.sortData(server.getServer(), data);
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
}