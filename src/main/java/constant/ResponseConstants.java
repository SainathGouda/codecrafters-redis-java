package constant;

public class ResponseConstants {
    public static final String PONG = "PONG";
    public static final String OK = "OK";
    public static final String QUEUED = "QUEUED";
    public static final String FULLRESYNC = "FULLRESYNC";
    public static final String ERROR = "ERR wrong number of arguments";
    public static final String INVALID_STREAM = "ERR Missing or invalid streams argument";
    public static final String MISMATCH_STREAM = "ERR Mismatched number of streams and IDs";
    public static final String NUMBER_FORMAT_EXCEPTION="ERR value is not an integer or out of range";
    public static final String EXEC_WITHOUT_MULTI="ERR EXEC without MULTI";
    public static final String DISCARD_WITHOUT_MULTI="ERR DISCARD without MULTI";
    public static final String NO_SUCH_FILE="ERR no such file";
    public static final String CANNOT_READ_DB_FILE="ERR error reading database file";
}
