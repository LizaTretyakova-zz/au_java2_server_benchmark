import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class SingleThreadServer extends BaseServer {

    private static final Logger LOGGER = Logger.getLogger("SingleThreadServer");
    private ServerSocket server;

    private final Thread workThread = new Thread(() -> {
        while(!Thread.interrupted()) {
//            try {
//                Socket client = server.accept();
//                DataInputStream input = new DataInputStream(client.getInputStream());
//                DataOutputStream output = new DataOutputStream(client.getOutputStream());
//
            Utils.tryConnectWithResourcesAndDoJob(server, (input, output) -> {
                        int size = input.readInt();
/*                byte[] message = new byte[size];*/
                        // reading the message
                        BenchmarkMessage.Array message = BenchmarkMessage.Array.parseFrom(input);
/*
 * Reading bytes instead of message. Not needed for now.
                if(input.read(message) != size) {
                    RuntimeException e = new RuntimeException("Couldn't read the message");
                    if(workThreadException == null) {
                        workThreadException = e;
                    }
                    throw e;
                }
*/
                        // reading the length of inputted data
                        int length = message.getSize();
                        // user passed array
                        int[] array = new int[length];
                        for (int i = 0; i < length; i++) {
                            array[i] = message.getArray(i);
                        }
                        serverJob(array);

                        // replying message
                        BenchmarkMessage.Array.Builder reply = BenchmarkMessage.Array.newBuilder().setSize(length);
                        for (int i = 0; i < length; i++) {
                            reply.setArray(i, array[i]);
                        }
                        reply.build().writeTo(output);
                    });
//            } catch (IOException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
        }
    });

    @Override
    public void start() throws IOException {
        server = new ServerSocket(PORT);
        workThread.start();
    }

    @Override
    public void stop() throws IOException {
        server.close();
        workThread.interrupt();
    }
}
