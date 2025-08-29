package data;

import command.CommandParser;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

class SortedSet {
    String member;
    double score;

    public SortedSet(String member, double score) {
        this.member = member;
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public String getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "SortedSet{" + "member='" + member + '\'' + ", score=" + score + '}';
    }
}

public class Storage {
    private final static ConcurrentHashMap<String, String> config = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, Socket> socketConfig = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, String> setValue = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, Long> expiry = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, List<String>> listValue = new ConcurrentHashMap<>();
    private final static ConcurrentHashMap<String, StreamCache> streamMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Thread, List<CommandParser.CommandWithArgs>> transactionMap = new ConcurrentHashMap<>();
    public final static CopyOnWriteArrayList<OutputStream> slaveOutputStreams = new CopyOnWriteArrayList<>();
    private final static ConcurrentHashMap<String, List<SortedSet>> zSet = new ConcurrentHashMap<>();

    public void setPort(int port) {
        config.put("port", String.valueOf(port));
    }

    public void setRole(String role) {
        config.put("role", role);
    }

    public String getRole() {
        return config.get("role");
    }

    public void setMasterAddress(String masterAddress) {
        config.put("masterAddress", masterAddress);
    }

    public void setMasterReplId(String masterReplId){
        config.put("master_replid", masterReplId);
    }

    public String getMasterReplId() {
        return config.get("master_replid");
    }

    public void setMasterReplOffset(String masterReplOffset){
        config.put("master_repl_offset", masterReplOffset);
    }

    public String getMasterReplOffset() {
        return config.get("master_repl_offset");
    }

    public void setDir(String dir){
        config.put("dir", dir);
    }

    public void setDbFileName(String dbFileName){
        config.put("db_file_name", dbFileName);
    }

    public String getRdbFileConfig(String arg) {
        return config.get(arg);
    }

    public void setClientSocket(Socket clientSocket){
        socketConfig.put("clientSocket", clientSocket);
    }

    public Socket getClientSocket() {
        return socketConfig.get("clientSocket");
    }

    public void setSlaveOutputStream(OutputStream outputStream){
        slaveOutputStreams.add(outputStream);
    }

    public CopyOnWriteArrayList<OutputStream> getSlaveOutputStreams(){
        return slaveOutputStreams;
    }

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

    public int getValueSize(){ return setValue.size(); }

    public ConcurrentHashMap<String, String> getValueKeySet() {
        return setValue;
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
    }

    public boolean multiExist(){
        return transactionMap.containsKey(Thread.currentThread());
    }

    public List<CommandParser.CommandWithArgs> execute(){
        return transactionMap.remove(Thread.currentThread());
    }

    public void addTransaction(CommandParser.CommandWithArgs commandWithArgs){
        transactionMap.get(Thread.currentThread()).add(commandWithArgs);
    }

    public void discardTransactions(){
        transactionMap.remove(Thread.currentThread());
    }

    //Sorted Sets
    public int addMember(String key, String member, double score){
        boolean wasAdded = true;
        SortedSet set = new SortedSet(member, score);

        List<SortedSet> sets = zSet.getOrDefault(key, new ArrayList<>());

        Iterator<SortedSet> iterator = sets.iterator();
        while (iterator.hasNext()) {
            SortedSet existingMember = iterator.next();
            if (existingMember.getMember().equals(member)) {
                iterator.remove();
                wasAdded = false;
                break;
            }
        }

        sets.add(set);
        zSet.put(key, sets);

        return wasAdded ? 1 : 0;
    }

    public int findMemberRanking(String key, String member){
        List<SortedSet> sets = zSet.getOrDefault(key, new ArrayList<>());
        sets = sets.stream()
                .sorted(Comparator.comparingDouble(SortedSet::getScore)
                        .thenComparing(SortedSet::getMember))
                .toList();

        int rank = 0;
        for (SortedSet existingMember : sets) {
            if (existingMember.getMember().equals(member)) {
                return rank;
            }
            rank++;
        }

        return -1;
    }
}
