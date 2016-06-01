import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NonblockingServer extends BaseTCPServer {

    public static final int INT_SIZE = 4;

    private Selector selector = null;
    private ServerSocketChannel serverSocketChannel = null;
    byte[] src;
//    private Map<SocketChannel, ByteBuffer> readingBuffers = new HashMap<>();
//    private Map<SocketChannel, ByteBuffer> writingBuffers = new HashMap<>();

    @Override
    public void start() {
        workThread.start();
    }

    @Override
    public void stop() throws IOException, InterruptedException {
        serverSocketChannel.close();
        workThread.interrupt();
        workThread.join();
    }

    @Override
    protected void processClient() {
        try {
            while (serverSocketChannel.isOpen()) {
                selector.select();

                for (Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                     keyIterator.hasNext(); ) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.socket().setTcpNoDelay(false);
                        client.register(
                                selector,
                                SelectionKey.OP_READ
                        );
//                        readingBuffers.put(client, ByteBuffer.allocate(src.length));
//                        writingBuffers.put(client, ByteBuffer.allocate(src.length));
//                    } else  if(key.isWritable()) {
//                        SocketChannel channel = (SocketChannel) key.channel();
//                        ByteBuffer channelBuffer = writingBuffers.get(channel);
//
//                        writeClient(channel, channelBuffer);

/* TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
 * TODO Что делать дальше, если надо прочитать чиселко и сообщение, обработать, а потом отправить обратно? Это неблокирующий сервер.
 *
 * Пихаем клиентский канал обратно в селектор с пометкой "ждём readable"
 * Когда получим readable канал - считываем из него в буфер, сколько получится.
 * Если ещё осталось место в буфере (видимо, если число, то буфер будет размером 4 байта) - обратно в readable
 * Иначе обрабатываем поступившие данные, создаём буфер, который хотим отправить в ответ и пихаем канал в selector
 * с пометкой "ждём writable". Когда будет writable - пишем из буфера в канал, сколько получится.
 * Если всё дописалось - закрываем, иначе опять в writable
 * */
                    } else if(key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer messageSize = ByteBuffer.allocate(INT_SIZE);
                        while(messageSize.remaining() > 0) {
                            client.read(messageSize);
                        }

                        int size = messageSize.getInt();
                        ByteBuffer message = ByteBuffer.allocate(size);
                        while(message.remaining() > 0) {
                            client.read(message);
                        }

                        BenchmarkMessage.Array protoMessage = BenchmarkMessage.Array.parseFrom(message.array());
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void writeClient(SocketChannel client, ByteBuffer clientBuffer) throws IOException {
        int offset = clientBuffer.position();
        clientBuffer.put(src, offset, src.length - offset);
        if(clientBuffer.remaining() <= 0) {
            client.close();
        }
    }
}
