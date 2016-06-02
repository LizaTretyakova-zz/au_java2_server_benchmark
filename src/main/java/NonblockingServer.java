import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class NonblockingServer extends BaseServer {

    private static final Logger LOGGER = Logger.getLogger("Nonblocking");
    private ExecutorService threadPool = Executors.newFixedThreadPool(10); // how many?
    private ServerSocketChannel serverChannel;

    private final Thread workThread = new Thread(() -> {
        try (
//                ServerSocketChannel serverChannel = ServerSocketChannel.open();
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
                     selectionKeyIterator.hasNext();) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (selectionKey.isAcceptable()) {
                        SocketChannel clientChannel = serverChannel.accept();
                        if (clientChannel != null) {
                            clientChannel.configureBlocking(false);
                            NonBlockingHandler client = new NonBlockingHandler(clientChannel);
                            int interest = client.onAcceptable();
                            if (interest != 0) {
                                // use attachment for a convenient further getting of client
                                clientChannel.register(selector, interest, client);
                            }
                        }
                    } else {
//                        threadPool.submit(() -> {
//                            try {
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
//                            } catch (ClosedChannelException e) {
//                                LOGGER.warning("Client left unexpectedly");
//                            }
//                            catch (IOException e) {
//                                e.printStackTrace();
//                                LOGGER.warning("IOException");
//                                throw new RuntimeException(e);
//                            }
////                        });
                    }
                }
            }
        } catch (SocketException e) {
            Logger.getAnonymousLogger().warning("ServerChannel closed or thread interrupted");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    });

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
    public void start() {
        workThread.start();
    }

    @Override
    public void stop() throws InterruptedException {
        workThread.interrupt();
        workThread.join();
    }

    @Override
    protected void processClient() {}

    public class NonBlockingHandler
            implements NonBlockingBase {
        private final SocketChannel client;
        private ByteBuffer clientBuffer = ByteBuffer.allocate(4);
        private int size = -1;

        public NonBlockingHandler(SocketChannel client) {
            this.client = client;
        }

        @Override
        public int onAcceptable() {
            return SelectionKey.OP_WRITE;

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

                return SelectionKey.OP_WRITE;
            }
        }

        @Override
        public int onWritable() throws IOException {
            client.write(clientBuffer);
            if (clientBuffer.remaining() > 0) {
                return SelectionKey.OP_WRITE;
            }
            clientBuffer = ByteBuffer.allocate(4);
            size = -1;
            return SelectionKey.OP_READ;
        }
    }

}
