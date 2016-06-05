package Servers;

import Utilities.Utils;

import java.io.IOException;

public class SingleThreadServer extends BaseTCPServer {

    @Override
    protected void processClient() {
        Utils.tryAcceptWithResourcesAndDoJob(server, this::processClientCore);
    }

    @Override
    public int getPort() {
        return 8084;
    }

    public static int getMAPort() {
        return 8084 + BaseServer.ADDING;
    }
}
