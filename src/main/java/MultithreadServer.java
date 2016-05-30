import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

public class MultithreadServer extends BaseServer {

    @Override
    public void start() throws IOException {
        server = new ServerSocket(PORT);
        workThread.start();
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        server.close();
        if(workThreadException != null) {
            throw workThreadException;
        }
        workThread.interrupt();
        workThread.join();
    }

    @Override
    protected void processClient(DataInputStream input, DataOutputStream output) {
        new Thread(() -> {
            processClientCore(input, output);
        }).start();
    }
}
