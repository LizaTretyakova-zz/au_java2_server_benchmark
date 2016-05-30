import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.logging.Logger;

public class SingleThreadServer extends BaseServer {

    private static final Logger LOGGER = Logger.getLogger("SingleThreadServer");

//    private final Thread workThread = new Thread(() -> {
//        while(!Thread.interrupted()) {
//            Utils.tryAcceptWithResourcesAndDoJob(server, (input, output) -> {
//                try {
//                    BenchmarkMessage.Array message = Utils.getMessage(input);
//
//                    // user passed array
//                    List<Integer> array = message.getArrayList();
//                    array = serverJob(array);
//
//                    // replying message
//                    Utils.sendMessage(output, array);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    if(workThreadException == null) {
//                        workThreadException = e;
//                    }
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//    });

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
    protected void processClient(DataInputStream input, DataOutputStream output) {
//        try {
//            BenchmarkMessage.Array message = Utils.getMessage(input);
//
//            // user passed array
//            List<Integer> array = message.getArrayList();
//            array = serverJob(array);
//
//            // replying message
//            Utils.sendMessage(output, array);
//        } catch (IOException e) {
//            e.printStackTrace();
//            if(workThreadException == null) {
//                workThreadException = e;
//            }
//            throw new RuntimeException(e);
//        }
        processClientCore(input, output);
    }
}
