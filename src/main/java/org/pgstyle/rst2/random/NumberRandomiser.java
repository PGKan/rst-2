package org.pgstyle.rst2.random;

import java.nio.ByteBuffer;

import org.pgstyle.rst2.security.RandomInputStream;
import org.pgstyle.rst2.security.Randomiser;
import org.pgstyle.rst2.security.SecureRandomInputStream;

public class NumberRandomiser extends Randomiser {

    public static NumberRandomiser getInstance() {
        return NumberRandomiser.getInstance(RandomUtils.randomLongSeed());
    }

    public static NumberRandomiser getInstance(long seed) {
        return new NumberRandomiser(new RandomInputStream(seed));
    }

    public static NumberRandomiser getInstance(String seed) {
        return NumberRandomiser.getInstance(RandomUtils.toLongSeed(seed));
    }

    public static NumberRandomiser getInstanceSecure() {
        return NumberRandomiser.getInstanceSecure(RandomUtils.randomBytesSeed());
    }

    public static NumberRandomiser getInstanceSecure(byte[] seed) {
        return new NumberRandomiser(new SecureRandomInputStream(seed));
    }

    @Deprecated
    public static NumberRandomiser getInstanceSecure(long seed) {
        return NumberRandomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public static NumberRandomiser getInstanceSecure(String seed) {
        return NumberRandomiser.getInstanceSecure(RandomUtils.toBytesSeed(seed));
    }

    public NumberRandomiser(RandomInputStream randomStream) {
        super(randomStream);
    }

    private double gaussian;
    private boolean haveGaussian = false;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean reset() {
        if (this.getRandomStream().resetSupported()) {
            this.haveGaussian = false;
            return super.reset();
        }
        return false;
    }

    public boolean generateBit() {
        return this.generateByte() < 0;
    }

    public byte generateByte() {
        return this.generate(1)[0];
    }

    public double generateDouble() {
        return (this.generateLong() >>> 11) / (double) (1l << 53);
    }

    public float generateFloat() {
        return (this.generateInteger() >>> 8) / (float) (1 << 24);
    }

    public int generateInteger() {
        return ByteBuffer.wrap(this.generate(4)).getInt();
    }

    public long generateLong() {
        return ByteBuffer.wrap(this.generate(8)).getLong();
    }

    public short generateShort() {
        return ByteBuffer.wrap(this.generate(2)).getShort();
    }

    public double generateGaussian() {
        if (haveGaussian) {
            haveGaussian = false;
            return gaussian;
        } else {
            double v1, v2, s;
            do {
                v1 = 2 * this.generateDouble() - 1; // between -1.0 and 1.0
                v2 = 2 * this.generateDouble() - 1; // between -1.0 and 1.0
                s = v1 * v1 + v2 * v2;
            } while (s >= 1 || s == 0);
            double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
            gaussian = v2 * multiplier;
            haveGaussian = true;
            return v1 * multiplier;
        }
    }

    @Override
    public String toString() {
        return "rst/NumberRandomiser:" + this.getRandomStream();
    }

}
