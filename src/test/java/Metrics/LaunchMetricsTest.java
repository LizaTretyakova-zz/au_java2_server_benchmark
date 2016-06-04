package Metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

public class LaunchMetricsTest {

    private final Logger LOGGER = LogManager.getLogger(MetricsAggregator.class);
    private final Parameter n = new Parameter("n", 0, 9, 1);
    private final Parameter m = new Parameter("m", 10, 10, 0);
    private final Parameter d = new Parameter("d", 10, 10, 0);
    private final MetricsAggregator ma = new MetricsAggregator(
            "test", 10, new Parameter("n", 1, 10, 1), new Parameter("m", 10, 10, 0), new Parameter("d", 10, 10, 0)
    );

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws IOException {
        new MetricsAggregatorTest().tearDown();
    }

    @Test
    public void testLaunch() throws Exception {

    }
}
