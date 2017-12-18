package org.logic.utils;

import org.logic.Constants;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Converter {

    private String uri;

    public Converter(String uri) {
        this.uri = uri;
    }

    public String calculate() {
        Mac sha512_HMAC = null;
        String result = null;
        String key = Constants.API_SECRET;

        try {
            byte[] byteKey = key.getBytes("UTF-8");
            final String HMAC_SHA256 = "HmacSHA512";
            sha512_HMAC = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA256);
            sha512_HMAC.init(keySpec);
            byte[] mac_data = sha512_HMAC.
                    doFinal(uri.getBytes("UTF-8"));
            //result = Base64.encode(mac_data);
            result = bytesToHex(mac_data);
//            System.out.println(result);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
//            System.out.println("Done");
        }
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
