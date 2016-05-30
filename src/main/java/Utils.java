import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class Utils {

    private static final Logger LOGGER = LogManager.getLogger("Utils");

    private Utils() {}

    public static List<Integer> tryConnectWithResourcesAndDoJob(
            ServerSocket server, BiFunction<DataInputStream, DataOutputStream, List<Integer>> job
    ) {
        try (
                Socket client = new Socket(server.getInetAddress(), server.getLocalPort());
                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
        ) {
            return job.apply(input, output);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void tryAcceptWithResourcesAndDoJob(
            ServerSocket server, BiConsumer<DataInputStream, DataOutputStream> job
    ) {
        try (
                Socket client = server.accept();
                DataInputStream input = new DataInputStream(client.getInputStream());
                DataOutputStream output = new DataOutputStream(client.getOutputStream());
        ) {
            job.accept(input, output);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static BenchmarkMessage.Array getMessage(DataInputStream input) throws IOException {
        int size = input.readInt();
        byte[] message = new byte[size];
        if(input.read(message) != size) {
            throw new IOException("Inconsistent message: size si not valid");
        }
        return BenchmarkMessage.Array.parseFrom(message);
    }

    public static void sendMessage(DataOutputStream output, List<Integer> array) throws IOException {
        BenchmarkMessage.Array message = BenchmarkMessage.Array
                .newBuilder()
                .setSize(array.size())
                .addAllArray(array)
                .build();
        int size = message.getSerializedSize();

        output.writeInt(size);
        message.writeTo(output);
        output.flush();
    }

    public static void tryAndDoJob(Socket socket, BiConsumer<DataInputStream, DataOutputStream> job) {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            job.accept(input, output);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void tryAcceptAndDoJob(ServerSocket server, BiConsumer<DataInputStream, DataOutputStream> job) {
        try {
            Socket socket = server.accept();
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            job.accept(input, output);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
