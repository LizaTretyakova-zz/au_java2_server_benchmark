import com.sun.deploy.security.ruleset.RunRule;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
    protected void processClient() {
        Utils.tryAcceptAndDoJob(server, (input, output) -> new Thread(() -> {
            processClientCore(input, output);
        }).start());

        // DO NOT DELETE THE BELOW CODE

//        try {
//            Socket socket = server.accept();
//            DataInputStream input = new DataInputStream(socket.getInputStream());
//            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
//
//            new Thread(() -> {
//                processClientCore(input, output);
//                try {
//                    socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    throw new RuntimeException(e);
//                }
//            }).start();
//        } catch (IOException e) {
//            LOGGER.info(e.getMessage());
//            throw new RuntimeException(e);
//        }

    }
}
