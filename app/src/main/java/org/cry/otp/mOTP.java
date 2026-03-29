/*
 * Copyright 2026 Chris Miceli and Michael Miceli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package org.cry.otp;

import java.util.Date;
import java.util.TimeZone;

/***
 * Description of algorithm: <a href="https://motp.sourceforge.net/">SourceForge</a>
 */
class mOTP {
    public static String gen(String PIN, String seed, String zone) {
        long time = new Date().getTime();
        String epoch = "" + (time + TimeZone.getTimeZone(zone).getOffset(time));
        String secret = new MD5(seed).asHex().substring(0, 16);
        epoch = epoch.substring(0, epoch.length() - 4);
        String otp = epoch + secret + PIN;
        MD5 hash = new MD5(otp);
        otp = hash.asHex().substring(0, 6);
        return otp;
    }
}
