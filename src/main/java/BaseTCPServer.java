import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
