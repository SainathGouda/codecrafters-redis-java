package data;

import java.util.List;

class StreamEntry {
    private final String id;
    private final List<String> keyValuePairs;

    public StreamEntry(String id, List<String> keyValuePairs) {
        this.id = id;
        this.keyValuePairs = keyValuePairs;
    }

    public String getId() { return id; }

    public List<String> getKeyValuePairs() { return keyValuePairs; }
}
