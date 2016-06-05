package Servers;

import Utilities.Utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadpoolServer extends BaseTCPServer {

    ExecutorService threadpool = Executors.newCachedThreadPool();

    @Override
    public void stop() throws IOException, InterruptedException {
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close();
        threadpool.shutdown();
//        threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        threadpool.awaitTermination(10, TimeUnit.SECONDS);
        workThread.interrupt();
        workThread.join();
        workThread = null;
        ma = null;
    }

    @Override
    protected void processClient() {
        Utils.tryAcceptAndDoJob(server, (input, output) -> threadpool.submit(() -> {
            while (true) {
                processClientCore(input, output);
            }
        }));
    }

    @Override
    public int getPort() {
        return 8085;
    }

    public static int getMAPort() {
        return 8085 + BaseServer.ADDING;
    }
}
