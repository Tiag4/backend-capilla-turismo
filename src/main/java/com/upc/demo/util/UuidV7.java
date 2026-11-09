package com.upc.demo.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TIMESTAMP_MASK = 0xFFFFFFFFFFFFL;

    private UuidV7() {
    }

    public static UUID generate() {
        long timestamp = System.currentTimeMillis() & TIMESTAMP_MASK;
        long randA = RANDOM.nextLong() & 0x0FFFL;
        long randB = RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL;

        long msb = (timestamp << 16) | 0x7000L | randA;
        long lsb = 0x8000000000000000L | randB;
        return new UUID(msb, lsb);
    }
}
