package constant;

public class RESPConstants {
    public static final String CRLF = "\r\n";
    public static final String SIMPLE_STRING_PREFIX = "+";
    public static final String ERROR_PREFIX = "-";
    public static final String INTEGER_PREFIX = ":";
    public static final String BULK_STRING_PREFIX = "$";
    public static final String ARRAY_PREFIX = "*";
    public static final String NULL_BULK_STRING = "$-1\r\n";
    public static final String NULL_ARRAY_STRING = "*-1\r\n";
    public static final String SET = "*3\r\n$3\r\nSET\r\n";
}
