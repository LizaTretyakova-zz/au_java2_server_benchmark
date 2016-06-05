package Metrics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ServerMetricsAggregator implements BaseMetricsAggregator {
    private final int port;
    private final Thread serverThread;
    private final List<Socket> clients = Collections.synchronizedList(new ArrayList<>());
    private final Logger LOGGER = LogManager.getLogger(ServerMetricsAggregator.class);

    public ServerMetricsAggregator(int port) throws IOException {
        this.port = port;
        ServerSocket server = new ServerSocket(port);
        serverThread = new Thread(() -> {
            while(true) {
                Socket socket = null;
                try {
                    socket = server.accept();
                    LOGGER.error("Accepted a client");
                } catch (IOException e) {
                    LOGGER.error("SMA connection closed");
                    return;
                }
                clients.add(socket);
            }
        });
        serverThread.start();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void submitRequest(long val) {
        submitVal(REQUEST, val);
    }

    @Override
    public void submitClient(long val) {
        submitVal(CLIENT, val);
    }

    private void submitVal(int purpose, long val) {
        synchronized (clients) {
            LOGGER.error("Looking for clients");
            Iterator<Socket> i = clients.iterator(); // Must be in synchronized block
            while (i.hasNext()) {
                Socket client = i.next();
                if(client.isClosed()) {
                    LOGGER.error("Socket was closed");
                    i.remove();
                } else {
                    try {
                        LOGGER.error("Trying to output");
                        DataOutputStream output = new DataOutputStream(client.getOutputStream());
                        output.writeInt(purpose);
                        output.writeLong(val);
                        output.flush();
                        LOGGER.error("Flushed");
                    } catch (IOException e) {
                        LOGGER.error("Client left?", e);
                        try {
                            client.close();
                        } catch (IOException e1) {
                            LOGGER.error("Client left me twice?!", e1);
                        }
                        i.remove();
                    }
                }
            }
        }
    }
}
