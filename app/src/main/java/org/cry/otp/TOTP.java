/*
 * Copyright 2026 Chris Miceli and Michael Miceli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package org.cry.otp;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Implementation of TOTP: Time-Based One-Time Password Algorithm derived from RFC 6238.
 * Copyright (c) 2011 IETF Trust and the persons identified as the document authors.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, is permitted
 * pursuant to, and subject to the license terms contained in the Simplified BSD License set forth
 * in Section 4.c of the IETF Trust's Legal Provisions Relating to IETF Documents
 * (http://trustee.ietf.org/license-info)
 * This code was derived from IETF RFC 6238. Please reproduce this note if possible.
 * @author Johan Rydell, PortWise, Inc., Chris Miceli, Michael Miceli
 */
public class TOTP {

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
    private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
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
        for (int i = 0; i < ret.length; i++) {
             ret[i] = bArray[i+1];
        }

        return ret;
    }

    public static String gen(String key, long timeInSeconds, int returnDigits, int shaType, int timeInterval) {
        long T0 = 0;

        long T = (timeInSeconds - T0) / (long) timeInterval;

        // represent number of time intervals as 0-left padded hex in uppercase
        String time = String.format("%016X", T);

        StringBuilder result;
        byte[] hash;

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);

        byte[] k = hexStr2Bytes(key);

        String crypto;
        if (shaType == 0) {
            crypto = "HmacSHA1";
        } else if (shaType == 1) {
            crypto = "HmacSHA256";
        } else {
            // sha 512
            crypto = "HmacSHA512";
        }

        hash = hmac_sha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % (int) (Math.pow(10, returnDigits));

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < returnDigits) {
            result.insert(0, "0");
        }

        return result.toString();
    }
}