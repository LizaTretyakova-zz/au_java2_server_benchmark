import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Logger;

public class SingleThreadServer extends BaseServer {

    private static final Logger LOGGER = Logger.getLogger("SingleThreadServer");

    @Override
    public void start() throws IOException {
        server = new ServerSocket(PORT);
        workThread.start();
    }

    @Override
    public void stop() throws IOException {
        server.close();
        if(workThreadException != null) {
            throw workThreadException;
        }
        workThread.interrupt();
    }

    @Override
    protected void processClient() {
        Utils.tryAcceptWithResourcesAndDoJob(server, this::processClientCore);
    }
}
