package org.logic.encryption;

public class Base64Crypto {

    // encode data on your side using BASE64
    public static String encode(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return java.util.Base64.getEncoder().encodeToString(s.getBytes());
    }

    // Decode data on other side, by processing encoded data
    public static String decode(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return new String(java.util.Base64.getDecoder().decode(s));
    }
}
