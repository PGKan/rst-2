package org.pgstyle.rst2.security;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.pgstyle.rst2.random.RandomUtils;

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

    public SecureRandomInputStream() {
        this(RandomInputStream.SEED_GENERATOR);
    }

    public SecureRandomInputStream(byte[] seed) {
        super(new SecureRandom(seed), 0, SecureRandomInputStream.L_SRESET);
        this.secureSeed = seed;
    }

    @Deprecated
    public SecureRandomInputStream(long seed) {
        this(RandomUtils.toBytesSeed(seed));
    }

    public SecureRandomInputStream(SecureRandom random) {
        this(random.generateSeed(64));
    }

    public SecureRandomInputStream(SecureRandomInputStream randomStream) {
        this((SecureRandom) randomStream.random);
    }

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
