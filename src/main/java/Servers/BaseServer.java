package Servers;

import Metrics.MetricsAggregator;
import Utilities.BenchmarkMessage;
import Utilities.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseServer {

    public static final int PORT = 8081;
    public static final int PACKET_SIZE = 65536;

    protected IOException workThreadException = null;
    protected MetricsAggregator ma;

    public abstract void start(MetricsAggregator ma) throws IOException;
    public abstract void stop() throws IOException, InterruptedException;
    protected abstract void processClient();

    public final List<Integer> serverJob(List<Integer> array) {
        long requestEnd;
        long requestStart = System.currentTimeMillis();

        ArrayList<Integer> mutableList = new ArrayList<>(array);
        Collections.sort(mutableList);

        requestEnd = System.currentTimeMillis();
        ma.submitRequest(requestEnd - requestStart);

        return mutableList;
    }

    public ServerSocket getServer() {
        return null;
    }

    public InetAddress getAddr() {
        return null;
    }

    public int getPort() {
        return -1;
    }

    protected Thread createWorkThread() {
        return new Thread(() -> {
            while(!Thread.interrupted()) {
                processClient();
            }
        });
    }

    protected final void processClientCore (DataInputStream input, DataOutputStream output) {
        try {
            long clientStart;
            long clientEnd;

            BenchmarkMessage.Array message;
            try {
                message = Utils.getMessage(input);
                clientStart = System.currentTimeMillis();
            } catch (EOFException e) {
                return;
            }
            // user passed array
            List<Integer> array = message.getArrayList();
            array = serverJob(array);

            // replying message
            Utils.outputMessage(output, array);
            clientEnd = System.currentTimeMillis();

            // TODO
            // TODO
            // TODO-TO-DO-TODO-TO-DO-TODOOOOOOOOOO
            // TODO-DO-DO-DO
            ma.submitClient(clientEnd - clientStart);
        } catch (IOException e) {
            e.printStackTrace();
            if(workThreadException == null) {
                workThreadException = e;
            }
            throw new RuntimeException(e);
        }
    }
}
