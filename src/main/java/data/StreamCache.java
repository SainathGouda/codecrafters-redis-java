package data;

import java.util.ArrayList;
import java.util.List;

class StreamCache {
    private final List<StreamEntry> entries = new ArrayList<>();

    public void addEntry(String id, List<String> keyValuePairs) {
        entries.add(new StreamEntry(id, keyValuePairs));
    }

    public List<StreamEntry> getEntries() { return entries; }
}
