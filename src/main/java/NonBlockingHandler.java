import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

//public class NonBlockingHandler
//        implements NonBlockingBase {
//    private final SocketChannel client;
//    private ByteBuffer clientBuffer = ByteBuffer.allocate(4);
//    private int size = -1;
//
//    public NonBlockingHandler(SocketChannel client) {
//        this.client = client;
//    }
//
//    @Override
//    public int onAcceptable() {
//        return SelectionKey.OP_WRITE;
//
//    }
//
//    @Override
//    public int onReadable() throws IOException {
//        int read = -1;
//        try {
//            client.read(clientBuffer);
//        } catch (IOException e) {
//            read = -1;
//        }
//        if(read == -1) {
//            client.close();
//            return 0;
//        }
//        if (clientBuffer.remaining() > 0) {
//            return SelectionKey.OP_READ;
//        }
//        if (size < 0) {
//            String data = new String(clientBuffer.array(), clientBuffer.arrayOffset(), clientBuffer.position());
//            size = Integer.parseInt(data);
//            clientBuffer = ByteBuffer.allocate(size);
//            return SelectionKey.OP_READ;
//        } else {
//            // process message
//            BenchmarkMessage.Array message = BenchmarkMessage.Array.parseFrom(clientBuffer.array());
//            List<Integer> array = message.getArrayList();
//            array = serverJob(array);
//        }
//    }
//
//    @Override
//    public int onWritable() throws IOException {
//        client.write(clientBuffer);
//        if (clientBuffer.remaining() > 0) {
//            return SelectionKey.OP_WRITE;
//        }
//        client.close();
//        return 0;
//    }
//}
