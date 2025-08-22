package blocking;

import constant.Constants;
import data.Storage;
import util.RespParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

public class BlockingOperationsManager {
    private final PriorityBlockingQueue<BlockedClient> blockedClients;
    private final Storage storage;

    public BlockingOperationsManager(Storage storage) {
        this.storage = storage;
        this.blockedClients = new PriorityBlockingQueue<>(
                Constants.BLOCKED_CLIENTS_INITIAL_CAPACITY,
                Comparator.comparingLong(BlockedClient::getBlockTime)
        );
    }

    public void addBlockedClient(String key, double timeoutSeconds, BufferedWriter outputStream) {
        blockedClients.offer(new BlockedClient(key, timeoutSeconds, outputStream));
    }

    public void notifyBlockedClients(String key) throws IOException {
        synchronized (blockedClients) {
            Iterator<BlockedClient> it = blockedClients.iterator();

            while (it.hasNext()) {
                BlockedClient client = it.next();

                if (client.getKey().equals(key)) {

                    if (storage.getListLength(key) != 0) {
                        List<String> popped = storage.getBLpopList(key);
                        RespParser.writeArray(popped.size(), popped, client.getOutputStream());

                        it.remove();
                        return;
                    }
                }
            }
        }
    }

    public void checkTimedOutClients() throws IOException {
        synchronized (blockedClients) {
            Iterator<BlockedClient> it = blockedClients.iterator();
            while (it.hasNext()) {
                BlockedClient client = it.next();
                if (client.isTimedOut()) {
                    RespParser.writeNullBulkString(client.getOutputStream());
                    it.remove();
                }
            }
        }
    }
}