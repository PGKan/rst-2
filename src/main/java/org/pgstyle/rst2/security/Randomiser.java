package org.pgstyle.rst2.security;

import java.io.Closeable;
import java.util.Objects;

public abstract class Randomiser implements Closeable {

    protected Randomiser(RandomInputStream randomStream) {
        this(1, randomStream);
    }

    protected Randomiser(int wordSize, RandomInputStream randomStream) {
        Objects.requireNonNull(randomStream, "randomStream == null");
        if (wordSize < 1) {
            throw new IllegalArgumentException("invalid word size: " + wordSize);
        }
        this.wordSize = wordSize;
        this.randomStream = randomStream;
    }

    private final RandomInputStream randomStream;
    private final int wordSize;

    @Override
    public void close() {
        this.getRandomStream().close();
    }

    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.randomStream.hashCode() * (wordSize + 1l));
    }

    public boolean reset() {
        if (this.getRandomStream().resetSupported()) {
            this.getRandomStream().reset();
            return true;
        }
        return false;
    }

    public byte[] generate(int length) {
        int size = length * this.getWordSize();
        byte[] bytes = new byte[Math.max(0, size)];
        for (int offset = 0; offset < size; offset += this.getRandomStream().read(bytes, offset, size - offset));
        return bytes;
    }

    public boolean skip(int length) {
        return this.generate(length).length / this.wordSize == length;
    }

    protected final RandomInputStream getRandomStream() {
        return this.randomStream;
    }

    protected final int getWordSize() {
        return this.wordSize;
    }

    public String toString() {
        return "rst+pglj/security/Randomiser:" + this.wordSize + "$" + this.getRandomStream().toString();
    }

}
