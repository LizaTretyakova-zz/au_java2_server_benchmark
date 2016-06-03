package Metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MetricsAggregatorTest {

    private MetricsAggregator ma;
    private final Logger LOGGER = LogManager.getLogger(MetricsAggregator.class);

    @Before
    public void setUp() throws Exception {
        ma = new MetricsAggregator(
                "test",
                100500,
                new Parameter("n", 0, 9, 1),
                new Parameter("m", 10, 10, 0),
                new Parameter("d", 10, 10, 0)
        );
    }

    @After
    public void tearDown() throws IOException {
        //Files.deleteIfExists(Paths.get(MetricsAggregator.NAME));
        Files.list(Paths.get(MetricsAggregator.NAME)).forEach((file) -> {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testStore() throws Exception {
        for(long i = 0; i < 10; i++) {
            ma.submit(i, i, i);
        }
        ma.store();
        LOGGER.error(Paths.get(MetricsAggregator.NAME));
        assertTrue(Files.exists(Paths.get(MetricsAggregator.NAME)));
    }

    @Test
    public void testDraw() throws Exception {
        testStore();
        ma.draw();
    }
}