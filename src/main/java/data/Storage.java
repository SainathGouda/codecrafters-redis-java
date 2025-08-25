package data;

import command.CommandParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Storage {
    private final static ConcurrentHashMap<String, String> setValue = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, List<String>> listValue = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, StreamCache> streamMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread, List<CommandParser.CommandWithArgs>> transactionMap = new ConcurrentHashMap<>();

    public void setData(String key, String value, long ttl) {
        setValue.put(key, value);
        expiry.put(key, ttl);
    }

    public void setList(String key, List<String> values) {
        if (listValue.containsKey(key)) {
            values.addAll(getList(key));
        }
        listValue.put(key, values);
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

        if (!listValue.containsKey(key) || listStartIndex > listEndIndex || listStartIndex >= listLength) {
            return new ArrayList<>();
        }

        listEndIndex = Math.min(listEndIndex+1, listLength);
        List<String> list = getList(key);
        return list.subList(listStartIndex, listEndIndex);
    }

    public long getExpiry(String key) {
        return expiry.getOrDefault(key, (long) -1);
    }

    public String getStoredType(String key) {
        if (getValue(key) != null) {
            return "string";
        }
        else if (getList(key) != null) {
            return "list";
        }
        else if(streamMap.containsKey(key)) {
            return "stream";
        }
        else {
            return "none";
        }
    }

    public void addStreamEntries(String streamKey, String entryId, List<String> streamEntries){
        StreamCache streamCache = streamMap.getOrDefault(streamKey, new StreamCache());
        streamCache.addEntry(entryId, streamEntries);
        streamMap.put(streamKey, streamCache);
    }

    public StreamCache getStreamCache(String streamKey){
        return streamMap.getOrDefault(streamKey, new StreamCache());
    }

    public void remove(String key) {
        setValue.remove(key);
        expiry.remove(key);
    }

    public String removeFromList(String key) {
        List<String> list = getList(key);
        String popped = list.removeFirst();
        listValue.put(key, list);

        return popped;
    }

    public List<String> removeFromList(String key, int removeCount) {
        List<String> list = getList(key);
        List<String> array = new ArrayList<>();
        for (int i=0; i<removeCount; i++) {
            String popped = list.removeFirst();
            array.add(popped);
        }
        listValue.put(key, list);

        return array;
    }

    private boolean isExpired(String key) {
        long ttl = expiry.getOrDefault(key, (long) -1);
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

    public int incr(String key){
        String value = getValue(key);
        long expiry = getExpiry(key);
        int intValue = 1;

        if (value != null) {
            try {
                intValue += Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        setData(key, String.valueOf(intValue), expiry);

        return intValue;
    }

    public void multi(){
        transactionMap.put(Thread.currentThread(), new ArrayList<>());
        System.out.println("Inside multi"+transactionMap.keySet());
    }

    public boolean multiExist(){
        System.out.println("Inside exist"+Thread.currentThread());
        return transactionMap.containsKey(Thread.currentThread());
    }

    public List<CommandParser.CommandWithArgs> execute(){
        System.out.println("Inside execute: "+transactionMap.keySet());
        return transactionMap.remove(Thread.currentThread());
    }

    public void addTransaction(CommandParser.CommandWithArgs commandWithArgs){
        System.out.println("Inside add: "+transactionMap.keySet());
        transactionMap.get(Thread.currentThread()).add(commandWithArgs);
    }

    public void discardTransactions(){
        transactionMap.remove(Thread.currentThread());
    }
}
