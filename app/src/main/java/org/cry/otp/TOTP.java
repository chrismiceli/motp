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
class TOTP {

    private TOTP() {
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     *
     * @param crypto   the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes the bytes to use for the HMAC key
     * @param text     the message or text to be authenticated.
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
     * @param hex the HEX string
     * @return A byte array
     */
    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    public static String gen(String key, int returnDigits, int shaType, int timeInterval) {
        long T0 = 0;

        long testTime = System.currentTimeMillis() / 1000L;
        long T = (testTime - T0) / (long) timeInterval;

        // will destroy T
        byte[] msg = new byte[8];
        for (int i = 7; i >= 0; --i) {
            msg[i] = (byte) (T & 0xff);
            T = T >> 8;
        }

        StringBuilder result;
        byte[] hash;

        byte[] k = new byte[key.length() / 2];
        for (int i = 0; i < key.length(); i += 2) {
            k[i / 2] = (byte) Integer.parseInt(key.substring(i, i + 2), 16);
        }

        String crypto;
        if (shaType == 0) {
            crypto = "HmacSHA1";
        } else if (shaType == 1) {
            crypto = "HmacSHA256";
        } else {
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

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < returnDigits) {
            result.append("0");
        }
        return result.toString();
    }
}