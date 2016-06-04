package Servers;

import Utilities.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MultithreadServer extends BaseTCPServer {

//    private final List<Thread> workers = Collections.synchronizedList(new ArrayList<>());

    @Override
    protected void processClient() {
        Utils.tryAcceptAndDoJob(server, (input, output) -> {
            Thread worker = new Thread(() -> {
                while(true) {
                    processClientCore(input, output);
                }
            });
//            workers.add(worker);
            worker.start();
        });
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close();

        workThread.interrupt();
//        synchronized (workers) {
//            for (Thread worker : workers) worker.join();
//        }
        workThread.join();
        workThread = null;
        ma = null;
    }
}
