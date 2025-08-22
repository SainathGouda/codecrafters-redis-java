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

    public void setListLeft(String key, List<String> values) {
        setList(key, values.reversed());
    }

    public String getValue(String key) {
        return setValue.get(key);
    }

    public List<String> getList(String key) {
        return listValue.get(key);
    }

    public int getListLength(String key) {
        return listValue.getOrDefault(key, new ArrayList<>()).size();
    }

    public List<String> getList(String key, int listStartIndex, int listEndIndex) {
        int listLength = getListLength(key);

        listStartIndex = (listStartIndex < 0) ? listStartIndex+listLength : listStartIndex;
        listEndIndex  = (listEndIndex < 0) ? listEndIndex+listLength : listEndIndex;

        listStartIndex = Math.max(0, listStartIndex);

        if (!this.listValue.containsKey(key) || listStartIndex > listEndIndex || listStartIndex >= listLength) {
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

    public String removeFromList(String key) {
        List<String> list = getList(key);
        String popped = list.removeFirst();
        this.listValue.put(key, list);

        return popped;
    }

    public List<String> removeFromList(String key, int removeCount) {
        List<String> list = getList(key);
        List<String> array = new ArrayList<>();
        for (int i=0; i<removeCount; i++) {
            String popped = list.removeFirst();
            array.add(popped);
        }
        this.listValue.put(key, list);

        return array;
    }

    public List<String> removeFromList(String key, long timeoutValue) {
        int listLength = getListLength(key);
        if (listLength==0){
            if (timeoutValue==0) {
                timeoutValue = Long.MAX_VALUE;
            }
            long waitTime = System.currentTimeMillis()+timeoutValue;
            while (System.currentTimeMillis()<waitTime) {
                if(getListLength(key)!=0) break;
            }
        }
        listLength = getListLength(key);
        if (listLength==0){
            return new ArrayList<>();
        }
        List<String> list = getList(key);
        String popped = list.removeFirst();
        this.listValue.put(key, list);

        List<String> array = new ArrayList<>();
        array.add(key);
        array.add(popped);
        return array;
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
