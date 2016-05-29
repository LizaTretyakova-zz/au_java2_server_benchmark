import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public final class Utils {

    private static final Logger LOGGER = Logger.getLogger("Utils");

    private Utils() {}

    public static void tryConnectWithResourcesAndDoJob(
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

}
