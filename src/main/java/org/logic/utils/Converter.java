package org.logic.utils;

import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.preferences.Params.API_SECRET_KEY;

public class Converter {

    private static final String HMAC_SHA256 = "HmacSHA512";
    private static SecretKeySpec secretKeySpec = null;
    private String uri;
    private Logger logger = Logger.getLogger(Converter.class);

    public Converter(String uri) throws UnsupportedEncodingException {
        this.uri = uri;
        this.secretKeySpec = generateSecretKey();
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

    private SecretKeySpec generateSecretKey() throws UnsupportedEncodingException {
        byte[] byteKey = API_SECRET_KEY.getBytes("UTF-8");
        return new SecretKeySpec(byteKey, HMAC_SHA256);
    }

    public String calculate() {
        Mac sha512_HMAC = null;
        String result = null;
        try {
            sha512_HMAC = Mac.getInstance(HMAC_SHA256);
            sha512_HMAC.init(secretKeySpec);
            byte[] mac_data = sha512_HMAC.
                    doFinal(uri.getBytes("UTF-8"));
            //result = Base64.encode(mac_data);
            result = bytesToHex(mac_data);
//            System.out.println(result);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
        } catch (InvalidKeyException e) {
            logger.error(e.getMessage());
        } finally {
//            System.out.println("Done");
        }
        return result;
    }
}
