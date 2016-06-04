package Servers;

import Metrics.MetricsAggregator;
import Utilities.BenchmarkMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonblockingServer extends BaseServer {

    private static final Logger LOGGER = LogManager.getLogger(NonblockingServer.class);
    private ExecutorService threadPool = Executors.newFixedThreadPool(10); // how many?
    private ServerSocketChannel serverChannel;
    private Thread workThread;

    @Override
    protected Thread createWorkThread() {
        return new Thread(() -> {
            try (
                    Selector selector = Selector.open()
            ) {
                serverChannel.bind(new InetSocketAddress(PORT));
                serverChannel.configureBlocking(false);
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);

                while (!Thread.interrupted()) {
                    selector.select();
                    if (Thread.interrupted()) {
                        break;
                    }

                    for (Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                         selectionKeyIterator.hasNext(); ) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();

                        if (selectionKey.isAcceptable()) {
                            SocketChannel clientChannel = serverChannel.accept();
                            long clientStart = System.currentTimeMillis();
                            if (clientChannel != null) {
                                clientChannel.configureBlocking(false);
                                NonBlockingHandler client = new NonBlockingHandler(clientChannel, clientStart);
                                int interest = client.onAcceptable();
                                if (interest != 0) {
                                    // use attachment for a convenient further getting of client
                                    clientChannel.register(selector, interest, client);
                                }
                            }
                        } else {
                            NonBlockingHandler client = (NonBlockingHandler) selectionKey.attachment();
                            SelectableChannel clientChannel = selectionKey.channel();

                            int interest = 0;
                            if (selectionKey.isReadable()) {
                                interest = client.onReadable();
                            } else if (selectionKey.isWritable()) {
                                interest = client.onWritable();
                            }
                            if (interest != 0) {
                                clientChannel.register(selector, interest, client);
                            } else {
                                selectionKey.cancel();
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                LOGGER.warn("ServerChannel closed or thread interrupted");
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public NonblockingServer() {
        try {
            serverChannel = ServerSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public ServerSocketChannel getServerChannel() {
        return serverChannel;
    }

    @Override
    public void start(MetricsAggregator metricsAggregator) {
        ma = metricsAggregator;
        workThread = createWorkThread();
        workThread.start();
    }

    @Override
    public void stop() throws InterruptedException {
        workThread.interrupt();
        workThread.join();
        workThread = null;
        ma = null;
    }

    @Override
    public ServerSocket getServer() {
        return serverChannel.socket();
    }

    @Override
    protected void processClient() {}

    public class NonBlockingHandler
            implements NonBlockingBase {
        private final SocketChannel client;
        private ByteBuffer clientBuffer = ByteBuffer.allocate(4);
        private int size = -1;
        private final long clientStart;
        private long requestStart;

        public NonBlockingHandler(SocketChannel client, long clientStart) {
            this.client = client;
            this.clientStart = clientStart;
        }

        @Override
        public int onAcceptable() {
            return SelectionKey.OP_READ;
        }

        @Override
        public int onReadable() throws IOException {
            int read = -1;
            try {
                read = client.read(clientBuffer);
            } catch (IOException e) {
                read = -1;
            }
            if(read == -1) {
                client.close();
                return 0;
            }
            if (clientBuffer.remaining() > 0) {
                return SelectionKey.OP_READ;
            }
            if (size < 0) {
                clientBuffer.flip();
                size = clientBuffer.getInt();
                clientBuffer = ByteBuffer.allocate(size);
                return SelectionKey.OP_READ;
            } else {
                // process message
                requestStart = System.currentTimeMillis();
                BenchmarkMessage.Array message = BenchmarkMessage.Array.parseFrom(clientBuffer.array());
                List<Integer> array = message.getArrayList();
                array = serverJob(array);

                BenchmarkMessage.Array response = BenchmarkMessage.Array
                        .newBuilder()
                        .setSize(array.size())
                        .addAllArray(array)
                        .build();
                int size = response.getSerializedSize();

                clientBuffer = ByteBuffer.allocate(4 + size);
                clientBuffer.putInt(size);
                clientBuffer.put(response.toByteArray());
                clientBuffer.flip();

                ma.submitRequest(System.currentTimeMillis() - requestStart);
                return SelectionKey.OP_WRITE;
            }
        }

        @Override
        public int onWritable() throws IOException {
            client.write(clientBuffer);
            if (clientBuffer.remaining() > 0) {
                return SelectionKey.OP_WRITE;
            }

            ma.submitClient(System.currentTimeMillis() - clientStart);
            clientBuffer = ByteBuffer.allocate(4);
            size = -1;
            return SelectionKey.OP_READ;
        }
    }

}
