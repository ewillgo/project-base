package cc.sportsdb.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public abstract class HashUtil {
    private static final java.security.SecureRandom random = new java.security.SecureRandom();
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final char[] CHAR_ARRAY = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final Map<String, MessageDigest> DIGEST_HOLDER = new HashMap<>();
    private static final Object lock = new Object();

    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA-1";
    private static final String SHA256 = "SHA-256";
    private static final String SHA384 = "SHA-384";
    private static final String SHA512 = "SHA-512";

    static {
        try {
            DIGEST_HOLDER.put(MD5, MessageDigest.getInstance(MD5));
            DIGEST_HOLDER.put(SHA1, MessageDigest.getInstance(SHA1));
            DIGEST_HOLDER.put(SHA256, MessageDigest.getInstance(SHA256));
            DIGEST_HOLDER.put(SHA384, MessageDigest.getInstance(SHA384));
            DIGEST_HOLDER.put(SHA512, MessageDigest.getInstance(SHA512));
        } catch (NoSuchAlgorithmException e) {
        }
    }


    public static String md5(String srcStr) {
        return hash(MD5, srcStr);
    }

    public static String sha1(String srcStr) {
        return hash(SHA1, srcStr);
    }

    public static String sha256(String srcStr) {
        return hash(SHA256, srcStr);
    }

    public static String sha384(String srcStr) {
        return hash(SHA384, srcStr);
    }

    public static String sha512(String srcStr) {
        return hash(SHA512, srcStr);
    }

    public static String hash(String algorithm, String srcStr) throws RuntimeException {
        try {
            if (DIGEST_HOLDER.get(algorithm) == null) {
                synchronized (lock) {
                    if (DIGEST_HOLDER.get(algorithm) == null) {
                        DIGEST_HOLDER.put(algorithm, MessageDigest.getInstance(algorithm));
                    }
                }
            }
            byte[] bytes = DIGEST_HOLDER.get(algorithm).digest(srcStr.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    /**
     * md5 128bit 16bytes
     * sha1 160bit 20bytes
     * sha256 256bit 32bytes
     * sha384 384bit 48bytes
     * sha512 512bit 64bytes
     */
    public static String generateSalt(int saltLength) {
        StringBuilder salt = new StringBuilder(saltLength);
        for (int i = 0; i < saltLength; i++) {
            salt.append(CHAR_ARRAY[random.nextInt(CHAR_ARRAY.length)]);
        }
        return salt.toString();
    }

    public static String generateSaltForSha256() {
        return generateSalt(32);
    }

    public static String generateSaltForSha512() {
        return generateSalt(64);
    }

    public static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }

        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
