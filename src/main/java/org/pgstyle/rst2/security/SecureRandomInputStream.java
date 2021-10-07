package org.pgstyle.rst2.security;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.pgstyle.rst2.random.RandomUtils;

/**
 * <p>
 * A {@code SecureRandomInputStream} is an
 * {@link java.io.InputStream InputStream} which can provides random bytes
 * data. It can be created use a seed in {@code long}, {@code String} or an
 * array of byte.
 * </p>
 * <p>
 * The random bytes are generated using a {@link SecureRandom} object, which
 * should be safe to use in most security required cases.
 * </p>
 *
 * @since rst-2
 * @version pgl-1.0/rst-2.0
 * @author PGKan
 */
public final class SecureRandomInputStream extends RandomInputStream {

    private static final Throwable L_SRESET;

    static {
        // test resetability
        Throwable reset = null;
        try {
            RandomInputStream.testResetability(new SecureRandom());
        }
        catch (RuntimeException e) {
            reset = e;
        }
        L_SRESET = reset;
    }

    /**
     * Creates a {@code SecureRandomInputStream} using an auto-generated seed.
     */
    public SecureRandomInputStream() {
        this(RandomInputStream.SEED_GENERATOR);
    }

    /**
     * Creates a {@code RandomInputStream} using provided seed.
     *
     * @param seed the seed for random byte generation
     * @throws NullPointerException if the seed is {@code null}
     */
    public SecureRandomInputStream(byte[] seed) {
        super(new SecureRandom(seed), 0, SecureRandomInputStream.L_SRESET);
        this.secureSeed = seed;
    }

    /**
     * Creates a {@code SecureRandomInputStream} using provided seed.
     *
     * @deprecated The underlying random instance in the
     *             {@code SecureRandomInputStream} is a {@code SecureRandom},
     *             which support seeding with an array of byte, consider to
     *             use {@link SecureRandomInputStream#SecureRandomInputStream(byte[])}
     *             instead.
     * @param seed the seed for random byte generation
     */
    @Deprecated
    public SecureRandomInputStream(long seed) {
        this(RandomUtils.toBytesSeed(seed));
    }

    /**
     * Creates a {@code SecureRandomInputStream} seed generated from the
     * provided random.
     *
     * @param random the random instance for generating the seed
     * @throws NullPointerException if the random is {@code null}
     */
    public SecureRandomInputStream(SecureRandom random) {
        this(random.generateSeed(64));
    }

    /**
     * Creates a {@code SecureRandomInputStream} seed generated from the
     * provided random stream.
     *
     * @param randomStream the random stream instance for generating the seed
     * @throws NullPointerException if the random stream is {@code null}
     */
    public SecureRandomInputStream(SecureRandomInputStream randomStream) {
        this((SecureRandom) randomStream.random);
    }

    /**
     * Creates a {@code RandomInputStream} using provided seed.
     *
     * @param seed the seed for random byte generation
     * @throws NullPointerException if the seed is {@code null}
     */
    public SecureRandomInputStream(String seed) {
        this(RandomUtils.toBytesSeed(seed));
    }

    private final byte[] secureSeed;

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Long.hashCode(Arrays.hashCode(this.secureSeed));
    }

    @Override
    public String toString() {
        return String.format("rst+pglj/security/SecureRandomInputStream:%s$%s",
                             this.random.getClass().getSimpleName(),
                             Base64.getUrlEncoder().encodeToString(this.secureSeed));
    }

}
