package Utilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class Utils {

    private static final Logger LOGGER = LogManager.getLogger("Utils");

    private Utils() {}

    // wrappers for connection establishing

    public static List<Integer> tryConnectWithResourcesAndDoJob(
            InetAddress addr, int port, BiFunction<DataInputStream, DataOutputStream, List<Integer>> job
    ) {
        try (
                Socket client = new Socket(addr, port);
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

        Socket client;
        try {
            client = server.accept();
        } catch (SocketException ignored) {
            LOGGER.info("Got SocketException during accept, assuming socket is closed");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        try (
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

    public static void tryAcceptAndDoJob(ServerSocket server, BiConsumer<DataInputStream, DataOutputStream> job) {
        try {
            Socket socket;
            try {
                socket = server.accept();
            } catch (SocketException e) {
                LOGGER.warn("Connection closed.");
                return;
            }
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            job.accept(input, output);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // sending messages

    public static BenchmarkMessage.Array getMessage(DataInputStream input) throws IOException {
        int size;
        size = input.readInt();
        byte[] message = new byte[size];
//        if(input.read(message) != size) {
//            throw new IOException("Inconsistent message: size is not valid");
//        }
        input.readFully(message);
        return BenchmarkMessage.Array.parseFrom(message);
    }

    public static void outputMessage(DataOutputStream output, List<Integer> array) throws IOException {
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
}
