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
private static byte[] hmac_sha(String crypto, byte[] keyBytes, byte[] text) {
    synchronized (hmac) {
        try {
            Mac hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }
}