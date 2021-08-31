package org.pgstyle.rst2.random;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

public final class RandomUtils {

    private static final MessageDigest    HASH;
    private static final NumberRandomiser RNG;

    static {
        // init HASH algorithm
        MessageDigest hash = null;
        String[] algorithms = {"SHA3-512", "SHA-512", "SHA3-256", "SHA-256", "SHA-1"};
        for (int i = 0; Objects.isNull(hash) && i < algorithms.length; i++) {
            try {
                hash = MessageDigest.getInstance(algorithms[i]);
            }
            catch (NoSuchAlgorithmException e) {
                // continue to try next algorithm
            }
        }
        HASH = Objects.requireNonNull(hash, "failed to obtain message digest algorithms");
        // init RNG
        RNG = NumberRandomiser.getInstanceSecure(new SecureRandom().generateSeed(64));
    }

    public static byte[] randomBytesSeed() {
        return RandomUtils.RNG.generate(64);
    }

    public static long randomLongSeed() {
        return RandomUtils.RNG.generateLong();
    }

    public static byte[] toBytesSeed(long seed) {
        byte[] bytes = new byte[8];
        for (int r = 8 - bytes.length % 8, i = bytes.length; i > 0; i--) {
            bytes[bytes.length - i] = (byte) (seed >>> ((i + r - 1) % 8) * 8 & 0xffl);
        }
        return bytes;
    }

    public static byte[] toBytesSeed(String seed) {
        Objects.requireNonNull(seed, "seed == null");
        byte[] bytes;
        synchronized (RandomUtils.HASH) {
            RandomUtils.HASH.reset();
            bytes = RandomUtils.HASH.digest(seed.getBytes(StandardCharsets.UTF_16BE));
            RandomUtils.HASH.reset();
        }
        return bytes;
    }

    public static long toLongSeed(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes == null");
        long seed = 0;
        for (int r = 8 - bytes.length % 8, i = bytes.length; i > 0; i--) {
            seed |= (bytes[bytes.length - i] & 0xffl) << (((i + r - 1) % 8) * 8);
        }
        return seed;
    }

    public static long toLongSeed(String seed) {
        return RandomUtils.toLongSeed(RandomUtils.toBytesSeed(seed));
    }

    private RandomUtils() {
        throw new UnsupportedOperationException("unnewable");
    }

}
