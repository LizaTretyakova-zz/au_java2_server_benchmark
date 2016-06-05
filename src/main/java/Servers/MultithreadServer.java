package Servers;

import Utilities.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MultithreadServer extends BaseTCPServer {

    @Override
    protected void processClient() {
        Utils.tryAcceptAndDoJob(server, (input, output) -> {
            Thread worker = new Thread(() -> {
                while(true) {
                    processClientCore(input, output);
                }
            });
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
        workThread.join();
        workThread = null;
        ma = null;
    }

    @Override
    public int getPort() {
        return 8081;
    }

    public static int getMAPort() {
        return 8081 + BaseServer.ADDING;
    }
}
