import java.io.IOException;

public interface NonBlockingBase {
    int onAcceptable();

    int onReadable() throws IOException;

    int onWritable() throws IOException;
}
