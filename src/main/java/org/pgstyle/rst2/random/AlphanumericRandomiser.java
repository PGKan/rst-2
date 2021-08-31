package org.pgstyle.rst2.random;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.pgstyle.rst2.security.RandomInputStream;
import org.pgstyle.rst2.security.Randomiser;
import org.pgstyle.rst2.security.SecureRandomInputStream;

public final class AlphanumericRandomiser extends Randomiser {

    public static AlphanumericRandomiser getInstance() {
        return AlphanumericRandomiser.getInstance(RandomUtils.randomLongSeed());
    }

    public static AlphanumericRandomiser getInstance(double ratio) {
        return AlphanumericRandomiser.getInstance(ratio, RandomUtils.randomLongSeed());
    }

    public static AlphanumericRandomiser getInstance(double ratio, long seed) {
        return new AlphanumericRandomiser(ratio, new RandomInputStream(seed));
    }

    public static AlphanumericRandomiser getInstance(double ratio, String seed) {
        return AlphanumericRandomiser.getInstance(ratio, RandomUtils.toLongSeed(seed));
    }

    public static AlphanumericRandomiser getInstance(long seed) {
        return new AlphanumericRandomiser(new RandomInputStream(seed));
    }

    public static AlphanumericRandomiser getInstance(String seed) {
        return AlphanumericRandomiser.getInstance(RandomUtils.toLongSeed(seed));
    }

    public static AlphanumericRandomiser getInstanceSecure() {
        return AlphanumericRandomiser.getInstanceSecure(RandomUtils.randomBytesSeed());
    }

    public static AlphanumericRandomiser getInstanceSecure(byte[] seed) {
        return new AlphanumericRandomiser(new SecureRandomInputStream(seed));
    }

    public static AlphanumericRandomiser getInstanceSecure(double ratio) {
        return AlphanumericRandomiser.getInstanceSecure(ratio, RandomUtils.randomBytesSeed());
    }

    public static AlphanumericRandomiser getInstanceSecure(double ratio, byte[] seed) {
        return new AlphanumericRandomiser(ratio, new SecureRandomInputStream(seed));
    }

    @Deprecated
    public static AlphanumericRandomiser getInstanceSecure(double ratio, long seed) {
        return AlphanumericRandomiser.getInstanceSecure(ratio, RandomUtils.toBytesSeed(seed));
    }

    public static AlphanumericRandomiser getInstanceSecure(double ratio, String seed) {
        return AlphanumericRandomiser.getInstanceSecure(ratio, RandomUtils.toBytesSeed(seed));
    }

    @Deprecated
    public static AlphanumericRandomiser getInstanceSecure(long seed) {
        return AlphanumericRandomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public static AlphanumericRandomiser getInstanceSecure(String seed) {
        return AlphanumericRandomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public AlphanumericRandomiser(RandomInputStream randomStream) {
        this(10.0 / 36, randomStream);
    }

    public AlphanumericRandomiser(double ratio, RandomInputStream randomStream) {
        super(randomStream);
        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("ratio out of bound: " + ratio);
        }
        this.ratio = ratio;
    }

    private final double ratio;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Double.hashCode(ratio);
    }

    @Override
    public byte[] generate(int length) {
        return this.generateString(length).getBytes(StandardCharsets.UTF_8);
    }

    public String generateString(int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            double pointer = this.getPointer();
            if (pointer < this.ratio) {
                string.append(this.getNumeric(pointer));
            }
            else {
                string.append(this.getAlphabet(pointer));
            }
        }
        return string.toString();
    }

    private int getNumeric(double pointer) {
        return (int) StrictMath.floor((pointer / this.ratio) * 10);
    }

    private char getAlphabet(double pointer) {
        return (char) StrictMath.floor((pointer - this.ratio) / (1.0 - this.ratio) * 26 + 'a');
    }

    private float getPointer() {
        return (ByteBuffer.wrap(super.generate(4)).getInt() >>> 8) / (float) (1 << 24);
    }

    @Override
    public String toString() {
        return String.format("rst/AlphanumericRandomiser:%s$%s", Double.toString(this.ratio), this.getRandomStream());
    }

}
