import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class BaseUDPServerTest {
    private final List<Integer> unsorted = Arrays.asList(3, 2, 4, 1, 5);
    private final List<Integer> sorted = Arrays.asList(1, 2, 3, 4, 5);

    public void baseTest(BaseUDPServer server, UDPClient client, List<Integer> data, List<Integer> expected)
            throws IOException, ExecutionException, InterruptedException {
        server.start();
        List<Integer> result = client.sortData(, data);
        server.stop();
        assertEquals(result, expected);
    }

}