package org.pgstyle.rst2.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

import org.pgstyle.rst2.random.RandomUtils;

public class RandomInputStream extends InputStream {

    private static final Throwable    RESET;
    protected static final SecureRandom SEED_GENERATOR;

    static {
        // init seed gen
        SEED_GENERATOR = new SecureRandom();
        // test resetability
        Throwable reset = null;
        try {
            RandomInputStream.testResetability(new Random());
        }
        catch (RuntimeException e) {
            reset = e;
        }
        RESET = reset;
    }

    protected static void testResetability(Random random) {
        long seed = random.nextLong();
        random.setSeed(seed);
        long first = random.nextLong();
        random.setSeed(seed);
        long second = random.nextLong();
        if (first != second) {
            throw new IllegalStateException("random provider does not support state reset", new IllegalArgumentException("head mismatch"));
        }
    }

    public RandomInputStream() {
        this(RandomInputStream.SEED_GENERATOR.nextLong());
    }

    public RandomInputStream(byte[] seed) {
        this(RandomUtils.toLongSeed(seed));
    }

    public RandomInputStream(long seed) {
        this.random = new Random(seed);
        this.seed = seed;
        this.failedReset = RandomInputStream.RESET;
    }

    public RandomInputStream(String seed) {
        this(RandomUtils.toLongSeed(seed));
    }

    protected RandomInputStream(Random random, long seed, Throwable reset) {
        Objects.requireNonNull(random, "random == null");
        this.random = random;
        this.seed = seed;
        this.failedReset = reset;
    }

    protected final Random    random;
    protected final long      seed;
    protected final Throwable failedReset;

    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void close() {
        // not supported
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return this.random.hashCode() ^ Long.hashCode(this.seed);
    }

    @Override
    public synchronized void mark(int readLimit) {
        // not supported
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() {
        byte[] bytes = new byte[1];
        this.random.nextBytes(bytes);
        return bytes[0] & 0xff;
    }

    @Override
    public int read(byte[] buffer) {
        return this.read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        Objects.requireNonNull(buffer, "buffer == null");
        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new IndexOutOfBoundsException("offset or length");
        }
        for (int i = 0; i < length; i++) {
            buffer[i + offset] = (byte) this.read();
        }
        return length;
    }

    @Override
    public synchronized void reset() {
        if (!this.resetSupported()) {
            throw new UnsupportedOperationException("underlying random does not support the reset of RandomStream", this.failedReset);
        }
        this.random.setSeed(this.seed);
    }

    public boolean resetSupported() {
        return Objects.isNull(this.failedReset);
    }

    @Override
    public long skip(long count) {
        try {
            return super.skip(count);
        }
        catch (IOException e) {
            // should not occur
            if (this.resetSupported()) {
                this.reset();
            }
            return -1;
        }
    }

    @Override
    public String toString() {
        return String.format("rst+pglj/security/RandomInputStream:%s$%s",
                             this.random.getClass().getSimpleName(),
                             Long.toHexString(this.seed));
    }

}
