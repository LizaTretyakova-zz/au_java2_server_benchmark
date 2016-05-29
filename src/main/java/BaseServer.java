import java.io.IOException;
import java.util.Arrays;

public abstract class BaseServer {
    public static final int PORT = 8081;

    public abstract void start() throws IOException;
    public abstract void stop() throws IOException;

    public final void serverJob(int[] array) {
        Arrays.sort(array);
    }
}
