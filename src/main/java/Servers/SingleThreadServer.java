package Servers;

import Utilities.Utils;

import java.io.IOException;

public class SingleThreadServer extends BaseTCPServer {

//    private static final Logger LOGGER = LogManager.getLogger("SingleThreadServer");

    @Override
    public void stop() throws IOException {
        //server.close();
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close();
        workThread.interrupt();
    }

    @Override
    protected void processClient() {
        Utils.tryAcceptWithResourcesAndDoJob(server, this::processClientCore);
    }
}
