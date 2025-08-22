package blocking;

import java.io.BufferedWriter;

public class BlockedClient {
    private final String key;
    private final BufferedWriter outputStream;
    private final long blockTime;
    private final long timeoutTime;

    public BlockedClient(String key, double timeoutSeconds, BufferedWriter outputStream) {
        this.key = key;
        this.outputStream = outputStream;
        this.blockTime = System.currentTimeMillis();
        this.timeoutTime = timeoutSeconds == 0 ? 0 : blockTime + (long) (timeoutSeconds * 1000);
    }

    public boolean isTimedOut() {
        return timeoutTime > 0 && System.currentTimeMillis() > timeoutTime;
    }

    public String getKey() {
        return this.key;
    }

    public BufferedWriter getOutputStream() {
        return this.outputStream;
    }

    public long getBlockTime() {
        return this.blockTime;
    }
}