package cc.sportsdb.common.util;


import java.nio.ByteBuffer;

public abstract class ByteUtil {

    private ByteUtil() {

    }

    public static byte[] longToBytes(long l) {
        return ByteBuffer.allocate(8).putLong(l).array();
    }

    public static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        return ByteBuffer.allocate(8).put(bytes).getLong(offset);
    }

    public static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static int bytesToInt(byte[] bytes) {
        return bytesToInt(bytes, 0);
    }

    public static int bytesToInt(byte[] bytes, int offset) {
        return ByteBuffer.allocate(4).put(bytes).getInt(offset);
    }

    public static byte[] shortToBytes(short s) {
        return ByteBuffer.allocate(2).putShort(s).array();
    }

    public static short bytesToShort(byte[] bytes) {
        return bytesToShort(bytes, 0);
    }

    public static short bytesToShort(byte[] bytes, int offset) {
        return ByteBuffer.allocate(2).put(bytes).getShort(offset);
    }

    public static byte[] charToBytes(char c) {
        return ByteBuffer.allocate(2).putChar(c).array();
    }

    public static char bytesToChar(byte[] bytes) {
        return bytesToChar(bytes, 0);
    }

    public static char bytesToChar(byte[] bytes, int offset) {
        return ByteBuffer.allocate(2).put(bytes).getChar(offset);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static long hexToLong(String hex) {
        return Long.parseLong(hex, 16);
    }

    public static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static short hexToShort(String hex) {
        return Short.parseShort(hex, 16);
    }

    public static String longToBinary(long l) {
        return Long.toBinaryString(l);
    }

    public static String intToBinary(int i) {
        return Integer.toBinaryString(i);
    }

}
