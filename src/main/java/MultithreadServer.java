import java.io.IOException;

public class MultithreadServer extends BaseTCPServer {

    @Override
    public void stop() throws IOException, InterruptedException {
        if(workThreadException != null) {
            throw workThreadException;
        }
        server.close(); // when you call server.close(), ALL subsequent server.accept() calls will throw SocketException
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
