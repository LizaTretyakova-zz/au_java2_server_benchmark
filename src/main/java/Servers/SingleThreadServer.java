package Servers;

import Utilities.Utils;

import java.io.IOException;

public class SingleThreadServer extends BaseTCPServer {

    @Override
    protected void processClient() {
        Utils.tryAcceptWithResourcesAndDoJob(server, this::processClientCore);
    }
}
