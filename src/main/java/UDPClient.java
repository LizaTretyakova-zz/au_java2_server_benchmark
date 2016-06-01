import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.*;

public class UDPClient {

    public List<Integer> sortData(InetAddress addr, int port, List<Integer> data)
            throws IOException, ExecutionException, InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(BaseServer.PACKET_SIZE);
        Utils.outputMessage(new DataOutputStream(output), data); // will it be written into the output or not?

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<List<Integer>> callable = () -> {
            DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), addr, port);
            try (
                    DatagramSocket socket = new DatagramSocket(0)
            ) {
                socket.send(packet);

                byte[] input = new byte[BaseServer.PACKET_SIZE];
                DatagramPacket response = new DatagramPacket(input, BaseServer.PACKET_SIZE);
                socket.receive(response);
                return Utils.getMessage(new DataInputStream(new ByteArrayInputStream(input))).getArrayList();
            }
        };
        Future<List<Integer>> future = executor.submit(callable);
        List<Integer> result = future.get();
        executor.shutdown();
        return result;
    }
}
