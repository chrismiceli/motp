/*
 * Copyright 2026 Chris Miceli and Michael Miceli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package org.cry.otp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class MD5 {
    private final String hex;

    public MD5(final String string) {
        String hexValue;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] hash = md.digest();
            StringBuilder buf = new StringBuilder(hash.length * 2);
            int i;

            for (i = 0; i < hash.length; i++) {
                if (((int) hash[i] & 0xff) < 0x10) {
                    buf.append("0");
                }

                buf.append(Long.toString((int) hash[i] & 0xff, 16));
            }

            hexValue = buf.toString();
        } catch (NoSuchAlgorithmException exception) {
            hexValue = "";
        }

        hex = hexValue;
    }

    public String asHex() {
        return hex;
    }
}