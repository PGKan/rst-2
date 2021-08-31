package org.pgstyle.rst2.random;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.pgstyle.rst2.security.RandomInputStream;
import org.pgstyle.rst2.security.Randomiser;
import org.pgstyle.rst2.security.SecureRandomInputStream;

public final class Base64Randomiser extends Randomiser {

    private static final Base64.Encoder ENCODER = Base64.getEncoder();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder();

    public static Base64Randomiser getInstance() {
        return Base64Randomiser.getInstance(RandomUtils.randomLongSeed());
    }

    public static Base64Randomiser getInstance(long seed) {
        return new Base64Randomiser(new RandomInputStream(seed));
    }

    public static Base64Randomiser getInstance(String seed) {
        return Base64Randomiser.getInstance(RandomUtils.toLongSeed(seed));
    }

    public static Base64Randomiser getInstanceSecure() {
        return Base64Randomiser.getInstanceSecure(RandomUtils.randomBytesSeed());
    }

    public static Base64Randomiser getInstanceSecure(byte[] seed) {
        return new Base64Randomiser(new SecureRandomInputStream(seed));
    }

    @Deprecated
    public static Base64Randomiser getInstanceSecure(long seed) {
        return Base64Randomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public static Base64Randomiser getInstanceSecure(String seed) {
        return Base64Randomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public Base64Randomiser(RandomInputStream randomStream) {
        super(randomStream);
    }

    @Override
    public byte[] generate(int length) {
        return this.generateUrlString(length).getBytes(StandardCharsets.UTF_8);
    }

    public String generateString(int length) {
        return Base64Randomiser.ENCODER.encodeToString(super.generate(length));
    }

    public String generateUrlString(int length) {
        return Base64Randomiser.URL_ENCODER.encodeToString(super.generate(length));
    }

    @Override
    public String toString() {
        return "rst/Base64Randomiser:" + this.getRandomStream();
    }

}
