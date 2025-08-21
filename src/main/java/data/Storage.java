package data;

import util.RespParser;

import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private final ConcurrentHashMap<String, String> value = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();

    public void setData(String key, String value, long ttl) {
        this.value.put(key, value);
        this.expiry.put(key, ttl);
    }

    public String getValue(String key) {
        return value.get(key);
    }

    public long getExpiry(String key) {
        return expiry.get(key);
    }

    public void remove(String key) {
        value.remove(key);
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
        return value.containsKey(key);
    }
}
