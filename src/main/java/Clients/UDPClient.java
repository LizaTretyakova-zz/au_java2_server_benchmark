package Clients;

import Metrics.MetricsAggregator;
import Servers.BaseServer;
import Utilities.Utils;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.*;

public class UDPClient extends BaseClient {

    @Override
    public List<Integer> sortData(InetAddress addr, int port, int x, int d, List<Integer> data, MetricsAggregator ma)
            throws IOException, ExecutionException, InterruptedException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(BaseServer.PACKET_SIZE);
        Utils.outputMessage(new DataOutputStream(output), data); // will it be written into the output or not?
        List<Integer> result = null;

        for (int i = 0; i < x; i++) {
            long start = System.currentTimeMillis();
            DatagramPacket packet = new DatagramPacket(output.toByteArray(), output.size(), addr, port);
            try (
                    DatagramSocket socket = new DatagramSocket()
            ) {
                socket.send(packet);

                byte[] input = new byte[BaseServer.PACKET_SIZE];
                DatagramPacket response = new DatagramPacket(input, BaseServer.PACKET_SIZE);
                socket.receive(response);
                result = Utils.getMessage(new DataInputStream(new ByteArrayInputStream(input))).getArrayList();
                ma.submitAvg(System.currentTimeMillis() - start);
            }
        }

        return result;
    }
}
