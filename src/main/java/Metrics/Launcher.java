package Metrics;

import Clients.BaseClient;
import Clients.TCPClient;
import Clients.UDPClient;
import Servers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Launcher {

    public static final String TCP_SINGLE = "SingleThreadServer";
    public static final String TCP_MULTI = "MultithreadServer";
    public static final String TCP_POOL = "ThreadpoolServer";
    public static final String TCP_NONBL = "NonblockingServer";
    public static final String UDP_MULTI = "MultithreadUDPServer";
    public static final String UDP_POOL = "ThreadpoolUDPServer";

    private static final Random RAND = new Random();
    private static final Logger LOGGER = LogManager.getLogger(Launcher.class);

    private final String arch;
    private final BaseServer server;
    private final Class<? extends BaseClient> clientClass;
//  private List<BaseClient> clients;
    private List<Integer> unsorted;
    private Parameter n;
    private Parameter m;
    private Parameter d;
    private int x;
    private MetricsAggregator ma;

    public Launcher(Parameter d, Parameter m, Parameter n, int x, String arch)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.d = d;
        this.m = m;
        this.n = n;
        this.x = x;
        this.arch = arch;

        ma = new MetricsAggregator(arch, x, n, m, d);

        switch(arch) {
            case TCP_SINGLE:
                server = new SingleThreadServer();
                clientClass = TCPClient.class;
                break;
            case TCP_MULTI:
                server = new MultithreadServer();
                clientClass = TCPClient.class;
                break;
            case TCP_POOL:
                server = new ThreadpoolServer();
                clientClass = TCPClient.class;
                break;
            case TCP_NONBL:
                server = new NonblockingServer();
                clientClass = TCPClient.class;
                break;
            case UDP_MULTI:
                server = new MultithreadUDPServer();
                clientClass = UDPClient.class;
                break;
            case UDP_POOL:
                server = new ThreadpoolUDPServer();
                clientClass = UDPClient.class;
                break;
            default:
                LOGGER.error("Unknown architecture. Terminating testing.");
                throw new RuntimeException("Unknown architecture. Terminating testing.");
        }
    }

    public MetricsAggregator launch()
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            IOException, ExecutionException, InterruptedException {
        server.start(ma);
        for(int i = 0; i < getSteps(); i++) {
            generateUnsorted(i);
            List<BaseClient> clients = generateClients(i);

            int a = 0;
            ExecutorService threadpool = Executors.newCachedThreadPool();
//            ExecutorService threadpool = Executors.newFixedThreadPool(50);
            for(BaseClient client: clients) {
                LOGGER.error(Integer.toString(a) + " new client in thread pool " + client.toString());
                a++;
                int iSnapshot = i;
                threadpool.submit( () -> {
                    try {
                        LOGGER.error("before sort " + client.toString());
                        client.sortData(
                                InetAddress.getByName("localhost"),
                                BaseServer.PORT,
                                x,
                                countParam(d, iSnapshot),
                                unsorted,
                                ma
                        );
                    } catch (InterruptedException | ExecutionException | IOException e) {
                        e.printStackTrace();
                        LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            }
            threadpool.shutdown();
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            ma.submit();
        }
        server.stop();
        return ma;
    }

    public void launchMultithread()
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            IOException, ExecutionException, InterruptedException {
        /* TODO */ server.start(ma);
        for(int i = 0; i < getSteps(); i++) {
            generateUnsorted(i);
            List<BaseClient> clients = generateClients(i);

            List<Integer> sorted = new ArrayList<>(unsorted);
            Collections.sort(sorted);

            ExecutorService threadpool = Executors.newCachedThreadPool();
            for(BaseClient client: clients) {
                int iSnapshot = i;
                threadpool.submit( () -> {
                    try {
                        requestServer(sorted, client, iSnapshot);
                    } catch (InterruptedException | ExecutionException | IOException e) {
                        e.printStackTrace();
                        LOGGER.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                });
            }
            threadpool.shutdown();
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        }
        /* TODO */ server.stop();
    }

    public void launchOneThread()
            throws
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException,
            IOException, ExecutionException, InterruptedException {
        /* TODO */ server.start(ma);
        for(int i = 0; i < getSteps(); i++) {
            generateUnsorted(i);
            List<BaseClient> clients = generateClients(i);

            List<Integer> sorted = new ArrayList<>(unsorted);
            Collections.sort(sorted);
            for(BaseClient client: clients) {
                requestServer(sorted, client, i);
            }
        }
        server.stop();
    }

    private void generateUnsorted(int i) {
        // int k = n.getStart() + i * n.getStep();
        int k = countParam(n, i);
        unsorted = new ArrayList<>();
        for(int j = 0; j < k; j++) {
            unsorted.add(j, RAND.nextInt());
        }
    }

    private List<BaseClient> generateClients(int i)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // int k = m.getStart() + i * m.getStep();
        int k = countParam(m, i);
        List<BaseClient> clients = new ArrayList<>();
        for(int j = 0; j < k; j++) {
            clients.add(j, clientClass.getConstructor().newInstance());
        }
        return clients;
    }

    private int countParam(Parameter p, int i) {
        return p.getStart() + i * p.getStep();
    }

    private int getSteps() {
        if(n.isChanging()) {
            return countSteps(n);
        }
        if(m.isChanging()) {
            return countSteps(m);
        }
        if(d.isChanging()) {
            return countSteps(d);
        }
        return 0;
    }

    private int countSteps(Parameter p) {
        return (p.getEnd() - p.getStart()) / p.getStep();
    }

    private void requestServer(List<Integer> sorted, BaseClient client, int i) throws IOException, ExecutionException, InterruptedException {
        List<Integer> result =
                client.sortData(InetAddress.getByName("localhost"), BaseServer.PORT, x, countParam(d, i), unsorted, ma);

        if (!sorted.equals(result)) {
            LOGGER.error("Expected: ");
            LOGGER.error(sorted);
            LOGGER.error("Got instead: ");
            LOGGER.error(result);
            throw new AssertionError("sorted != result, arch is " + arch);
        }
//        else {
//            LOGGER.error(arch + ": success");
//        }
    }
}
