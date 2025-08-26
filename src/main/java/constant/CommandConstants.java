package constant;

public class CommandConstants {
    public static final String PING = "PING";
    public static final String ECHO = "ECHO";
    public static final String SET = "SET";
    public static final String PX = "PX";
    public static final String GET = "GET";

    //Lists
    public static final String RPUSH = "RPUSH";
    public static final String LRANGE = "LRANGE";
    public static final String LPUSH = "LPUSH";
    public static final String LLEN = "LLEN";
    public static final String LPOP = "LPOP";

    //Streams
    public static final String TYPE = "TYPE";
    public static final String XADD = "XADD";
    public static final String XRANGE = "XRANGE";
    public static final String XREAD = "XREAD";

    //Transactions
    public static final String INCR = "INCR";
    public static final String MULTI = "MULTI";
    public static final String EXEC = "EXEC";
    public static final String DISCARD = "DISCARD";

    //Replications
    public static final String INFO = "INFO";
    public static final String REPLCONF = "REPLCONF";
    public static final String PSYNC = "PSYNC";
}
