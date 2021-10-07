package org.pgstyle.rst2.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

import org.pgstyle.rst2.random.RandomUtils;

/**
 * <p>
 * A {@code RandomInputStream} is an {@link InputStream} which can provides
 * random bytes data. It can be created use a seed in {@code long},
 * {@code String} or an array of byte.
 * </p>
 * <p>
 * The random bytes are generated using a {@link Random} object, which may
 * be not safe to use in security required cases, consider to use
 * {@code SecureRandomInputStream} instead.
 * </p>
 *
 * @since rst-2
 * @version pgl-1.0/rst-2.0
 * @author PGKan
 * @see SecureRandomInputStream
 */
public class RandomInputStream extends InputStream {

    private static final Throwable    RESET;
    /** A {@code SecureRandom} instance for default seed generation. */
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

    /**
     * Tests can a {@code Random} instance be reset.
     *
     * @param random random instance to be tested
     * @throws IllegalStateException if the random instance failed in resetting
     */
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

    /**
     * Creates a {@code RandomInputStream} using an auto-generated seed.
     */
    public RandomInputStream() {
        this(RandomInputStream.SEED_GENERATOR.nextLong());
    }

    /**
     * Creates a {@code RandomInputStream} using provided seed.
     *
     * @param seed the seed for random byte generation
     * @throws NullPointerException if the seed is {@code null}
     */
    public RandomInputStream(byte[] seed) {
        this(RandomUtils.toLongSeed(seed));
    }

    /**
     * Creates a {@code RandomInputStream} using provided seed.
     *
     * @param seed the seed for random byte generation
     */
    public RandomInputStream(long seed) {
        this.random = new Random(seed);
        this.seed = seed;
        this.failedReset = RandomInputStream.RESET;
    }

    /**
     * Creates a {@code RandomInputStream} using provided seed.
     *
     * @param seed the seed for random byte generation
     * @throws NullPointerException if the seed is {@code null}
     */
    public RandomInputStream(String seed) {
        this(RandomUtils.toLongSeed(seed));
    }

    /**
     * Creates a {@code RandomInputStream} with a {@code Random} instance and
     * its additional information.
     *
     * @param random the random instance
     * @param seed seed stored for resetting
     * @param reset throwable to be thrown when resetting the random stream
     * @throws NullPointerException if the random is {@code null}
     */
    protected RandomInputStream(Random random, long seed, Throwable reset) {
        Objects.requireNonNull(random, "random == null");
        this.random = random;
        this.seed = seed;
        this.failedReset = reset;
    }

    /** the random byte provider */
    protected final Random    random;
    /** the seed for resetting */
    protected final long      seed;
    /** the throwable for indicate failed reset */
    protected final Throwable failedReset;

    /**
     * Returns the number of bytes available for reading.
     *
     * @return {@code Integer.MAX_VALUE}
     */
    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    /**
     * The {@code RandomInputStream} can't be closed, thus calling this
     * method does nothing.
     */
    @Override
    public void close() {
        // not supported
    }

    /**
     * Returns {@code true} if the object {@code other} has the same reference
     * of this object. i.e. {@code this == other}
     *
     * @param other the object to be tested
     * @return {@code true} if the object {@code other} has the same reference
     *         of this object; or {@code false} otherwise
     */
    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    /**
     * Returns the hash code value of this random input stream.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return this.random.hashCode() ^ Long.hashCode(this.seed);
    }

    /**
     * The {@code RandomInputStream} can't be marked, thus calling this
     * method does nothing.
     *
     * @param readLimit unused
     */
    @Override
    public synchronized void mark(int readLimit) {
        // not supported
    }

    /**
     * The {@code RandomInputStream} can't be marked, thus this method always
     * return {@code false}.
     *
     * @return {@code false}
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Reads 1 byte out of the {@code RandomInputStream}.
     *
     * @return the byte value from 0 to 255
     */
    @Override
    public int read() {
        byte[] bytes = new byte[1];
        this.random.nextBytes(bytes);
        return bytes[0] & 0xff;
    }

    /**
     * Reads bytes into the given byte array.
     *
     * @param buffer a byte array for storing the read byte
     * @return the number of bytes have been read
     */
    @Override
    public int read(byte[] buffer) {
        return this.read(buffer, 0, buffer.length);
    }

    /**
     * Reads bytes into the specific partition in the given byte array.
     *
     * @param buffer a byte array for storing the read byte
     * @param offset the start position of the array can be used to store read
     *               byte
     * @param length the number of byte to be read
     * @return the number of bytes have been read
     * @throws IndexOutOfBoundsException if the offset or length is negative;
     *         or the sum of offset and length larger than the length of the array
     * @throws NullPointerException if the byte array is {@code null}
     */
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

    /**
     * Resets this {@code RandomInputStream} with the seed stored during
     * creation if reset is supported.
     *
     * @throws UnsupportedOperationException if the random instance does not
     *         support reset
     */
    @Override
    public synchronized void reset() {
        if (!this.resetSupported()) {
            throw new UnsupportedOperationException("underlying random does not support the reset of RandomStream", this.failedReset);
        }
        this.random.setSeed(this.seed);
    }

    /**
     * Returns {@code true} if this {@code RandomInputStream} can be reset.
     *
     * @return {@code true} if this {@code RandomInputStream} can be reset; or
     *         {@code false} otherwise
     */
    public boolean resetSupported() {
        return Objects.isNull(this.failedReset);
    }

    /**
     * Skips over bytes in the {@code RandomInputStream} and returns the
     * number of bytes skipped. If the skipping is failed, {@code -1} will be
     * returned and this method will try to reset the itself (can fail, but no
     * exception will be thrown).
     *
     * @return the number of bytes skipped; or {@code -1} if the skipping is
     *         failed
     */
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

    /**
     * Returns the string representation of this {@code RandomInputStream}
     * object
     *
     * @return the string representation of this {@code RandomInputStream}
     *         object
     */
    @Override
    public String toString() {
        return String.format("rst+pglj/security/RandomInputStream:%s$%s",
                             this.random.getClass().getSimpleName(),
                             Long.toHexString(this.seed));
    }

}
