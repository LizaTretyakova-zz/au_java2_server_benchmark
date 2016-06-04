package Servers;

import Utilities.Utils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadpoolServer extends BaseTCPServer {

    ExecutorService threadpool = Executors.newCachedThreadPool();

    @Override
    public void stop() throws IOException, InterruptedException {
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close();
        threadpool.shutdown();
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
}
