package Servers;

import Servers.BaseServer;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class BaseTCPServer extends BaseServer {
    protected ServerSocket server;
    protected IOException workThreadException = null;
    protected final Thread workThread = new Thread(() -> {
        while(!Thread.interrupted() && !server.isClosed()) {
            processClient();
        }
    });


    public static final int PORT = 8081;

    public abstract void stop() throws IOException, InterruptedException;
    protected abstract void processClient();

    public void start() throws IOException {
        server = new ServerSocket(PORT);
        workThread.start();
    }

    public ServerSocket getServer() {
        return server;
    }
}
