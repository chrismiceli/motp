package org.cry.otp;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This an example implementation of the OATH TOTP algorithm. Visit
 * www.openauthentication.org for more information. There have been
 * modifications to work within the mOTP framework for OTPs by Chris Miceli
 * 
 * @author Johan Rydell, PortWise, Inc.
 */
public class TOTP {

    private TOTP() {}

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     * 
     * @param crypto
     *            the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes
     *            the bytes to use for the HMAC key
     * @param text
     *            the message or text to be authenticated.
     */
    private static byte[] hmac_sha1(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    /**
     * This method converts HEX string to Byte[]
     * 
     * @param hex
     *            the HEX string
     * 
     * @return A byte array
     */
    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        for (int i = 0; i < ret.length; i++)
            ret[i] = bArray[i + 1];
        return ret;
    }

    public static String encodeHexString(String sourceText) {

        byte[] rawData = sourceText.getBytes();
        StringBuffer hexText = new StringBuffer();
        String initialHex = null;
        int initHexLength = 0;

        for (int i = 0; i < rawData.length; i++) {
            int positiveValue = rawData[i] & 0x000000FF;
            initialHex = Integer.toHexString(positiveValue);
            initHexLength = initialHex.length();
            while (initHexLength++ < 2) {
                hexText.append("0");
            }
            hexText.append(initialHex);
        }
        return hexText.toString();
    }

    public static String gen(String key, int returnDigits, int shaType,
            String timeZone, int timeInterval) {
        long T0 = 0;
        long X = timeInterval;// 30;

        long testTime = (long) (System.currentTimeMillis() / 1000L);
        String time = "0";

        long T = (testTime - T0) / X;
        time = Long.toHexString(T).toUpperCase();
        while (time.length() < 16)
            time = "0" + time;

        String result = null;
        byte[] hash;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);

        // Adding one byte to get the right conversion
        // key = encodeHexString(key);
        // byte[] k = hexStr2Bytes(key);

        byte[] k = new byte[key.length() / 2];
        for (int i = 0; i < key.length(); i += 2) {
            k[i / 2] = (byte) Integer.parseInt(key.substring(i, i + 2), 16);
        }

        String crypto;
        if (shaType == 0) {
            crypto = "HmacSHA1";
        }
        else if (shaType == 1) {
            crypto = "HmacSHA256";
        }
        else {
            // sha 256
            crypto = "HmacSHA512";
        }
        hash = hmac_sha1(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % (int) (Math.pow(10, returnDigits));

        result = Integer.toString(otp);
        while (result.length() < returnDigits) {
            result = "0" + result;
        }
        return result;
    }
}