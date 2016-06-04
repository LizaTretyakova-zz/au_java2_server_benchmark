package Metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

public class LauncherTest {

    private final Logger LOGGER = LogManager.getLogger(MetricsAggregator.class);
    private final Parameter n = new Parameter("n", 1000, 1000, 0);
    private final Parameter m = new Parameter("m", 20, 30, 1);
    private final Parameter d = new Parameter("d", 500, 500, 0);
    private final int x = 5;
    private final MetricsAggregator ma = new MetricsAggregator("test", x, n, m, d);

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws IOException {
        new MetricsAggregatorTest().tearDown();
    }

    public void baseTestStupid(String arch) {
        Launcher launcher = getLauncher(arch);
        try {
            launcher.launchOneThread();
        } catch (
                NoSuchMethodException
                        | IllegalAccessException
                        | InvocationTargetException
                        | IOException
                        | InstantiationException
                        | ExecutionException
                        | InterruptedException e
                ) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    @Test
//    public void testMultithreadStupid() {
//        baseTestStupid(Launcher.TCP_MULTI);
//    }

//    @Test
//    public void testThreadpoolStupid() {
//        baseTestStupid(Launcher.TCP_POOL);
//    }

//    @Test
//    public void testNonblockingStupid() {
//        baseTestStupid(Launcher.TCP_NONBL);
//    }

// TODO: cnother client
//    @Test
//    public void testSingleStupid() {
//        baseTestStupid(Launcher.TCP_SINGLE);
//    }

    @Test
    public void testUDPMultihreadStupid() {
        baseTestStupid(Launcher.UDP_MULTI);
    }

    @Test
    public void testUDPThreadpoolStupid() {
        baseTestStupid(Launcher.UDP_POOL);
    }

    public void baseTestMulti(String arch) {
        Launcher launcher;
        launcher = getLauncher(arch);
        try {
            launcher.launchMultithread();
        } catch (
                NoSuchMethodException
                        | IllegalAccessException
                        | InvocationTargetException
                        | IOException
                        | InstantiationException
                        | ExecutionException
                        | InterruptedException e
                ) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    @Test
//    public void testMultithread() {
//        baseTestMulti(Launcher.TCP_MULTI);
//    }
//
//    @Test
//    public void testThreadpool() {
//        baseTestMulti(Launcher.TCP_POOL);
//    }

    @Test
    public void testNonblocking() {
        baseTestMulti(Launcher.TCP_NONBL);
    }

//    @Test
//    public void testSingle() {
//        baseTestMulti(Launcher.TCP_SINGLE);
//    }

    @Test
    public void testUDPMultithread() {
        baseTestMulti(Launcher.UDP_MULTI);
    }

    @Test
    public void testUDPThreadpool() {
        baseTestMulti(Launcher.UDP_POOL);
    }

    private Launcher getLauncher(String arch) {
        Launcher launcher;
        try {
            launcher = new Launcher(d, m, n, x, arch);
        } catch (
                InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e
                ) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return launcher;
    }
}