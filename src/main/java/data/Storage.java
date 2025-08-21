package data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private final ConcurrentHashMap<String, String> setValue = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> listValue = new ConcurrentHashMap<>();

    public void setData(String key, String value, long ttl) {
        this.setValue.put(key, value);
        this.expiry.put(key, ttl);
    }

    public void setList(String key, List<String> values) {
        if (this.listValue.containsKey(key)) {
            values.addAll(getList(key));
        }
        this.listValue.put(key, values);
    }

    public String getValue(String key) {
        return setValue.get(key);
    }

    public List<String> getList(String key) {
        return listValue.get(key);
    }

    public int getListLength(String key) {
        return listValue.get(key).size();
    }

    public List<String> getList(String key, int listStartIndex, int listEndIndex) {
        if (!this.listValue.containsKey(key) || listStartIndex > listEndIndex) {
            return new ArrayList<>();
        }
        int listLength = getListLength(key);
        if (listStartIndex >= listLength ||  listEndIndex >= listLength) {
            return new ArrayList<>();
        }

        listEndIndex = Math.min(listEndIndex+1, listLength);
        List<String> list = getList(key);
        return list.subList(listStartIndex, listEndIndex);
    }

    public long getExpiry(String key) {
        return expiry.get(key);
    }

    public void remove(String key) {
        setValue.remove(key);
        expiry.remove(key);
    }

    private boolean isExpired(String key) {
        long ttl = expiry.get(key);
        if(ttl == -1) {
            return false;
        }
        return System.currentTimeMillis() > expiry.get(key);
    }

    public boolean hasKey(String key) {
        if (isExpired(key)) {
            remove(key);
            return false;
        }
        return setValue.containsKey(key);
    }
}
