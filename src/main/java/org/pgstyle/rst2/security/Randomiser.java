package org.pgstyle.rst2.security;

import java.io.Closeable;
import java.util.Objects;

/**
 * <p>
 * {@code Randomiser} is the super class of all randomiser implementations.
 * It provides basic manipulation of the underlying random input stream, like
 * {@code close}, {@code reset}, {@code generate(read)} and {@code skip}.
 * </p>
 * <p>
 * The {@code Randomiser} generates or skips with a pre determined word size,
 * each generate or skip operation will read or skip thought a given length
 * in term of words. For example, with the word size defined as 4 bytes, the
 * {@code generate(16)} method will return a total of 16 words or 64 bytes.
 * </p>
 * <p>
 * The {@code Randomiser} class is defined as {@code abstract}. However, all
 * methods have already been implemented. A simple anonymous class defining
 * a public constructor and instantiate a {@code Randomiser} object, but we
 * don't encourage this move.
 * </p>
 *
 * @since rst-2
 * @version pgl-1.0/rst-2.0
 * @author PGKan
 * @see RandomInputStream
 */
public abstract class Randomiser implements Closeable {

    /**
     * Initialise the {@code Randomiser} with a given random input stream. the
     * word size will be defaulted as 1.
     *
     * @param randomStream a random stream
     * @throws NullPointerException if the random stream is {@code null}
     */
    protected Randomiser(RandomInputStream randomStream) {
        this(1, randomStream);
    }

    /**
     * Initialise the {@code Randomiser} with a given random input stream and
     * word size.
     *
     * @param wordSize the number of bytes per generate or skip action
     * @param randomStream a random stream
     * @throws IllegalArgumentException if the word size is less than 1
     * @throws NullPointerException if the random stream is {@code null}
     */
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

    /**
     * Closes the random stream of this {@code Randomiser}.
     */
    @Override
    public void close() {
        this.getRandomStream().close();
    }

    /**
     * Returns {@code true} if the object {@code other} has the same reference
     * of this object. i.e. {@code this == other}
     *
     * @param object the object to be tested
     * @return {@code true} if the object {@code other} has the same reference
     *         of this object; or {@code false} otherwise
     */
    @Override
    public boolean equals(Object object) {
        return this == object;
    }

    /**
     * Returns the hash code value of this {@code Randomiser}.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Long.hashCode(this.randomStream.hashCode() * (wordSize + 1l));
    }

    /**
     * Resets the random stream of this {@code Randomiser}.
     *
     * @return {@code true} if reset is available and succeeded; or
     *         {@code false} otherwise
     */
    public boolean reset() {
        if (this.getRandomStream().resetSupported()) {
            this.getRandomStream().reset();
            return true;
        }
        return false;
    }

    /**
     * Generates random bytes with random values from the random stream of
     * this {@code Randomiser}. The size of the result array of byte equals to
     * the length multiplied by the word size of this {@code Randomiser}.
     *
     * @param length number of words to be generated
     * @return array of random bytes with size of {@code length * wordSize};
     *         or an empty array if the {@code length} given is negative
     */
    public byte[] generate(int length) {
        int size = length * this.getWordSize();
        byte[] bytes = new byte[Math.max(0, size)];
        for (int offset = 0; offset < size; offset += this.getRandomStream().read(bytes, offset, size - offset));
        return bytes;
    }

    /**
     * Skips over words in the random stream of this {@code Randomiser}.
     *
     * @param length number of words to be skipped
     * @return {@code true} if skip is succeeded completely; or {@code false}
     *         otherwise
     */
    public boolean skip(int length) {
        return this.generate(length).length / this.wordSize == length;
    }

    /**
     * Returns the underlying random stream of this {@code Randomiser}.
     *
     * @return the underlying random stream
     */
    protected final RandomInputStream getRandomStream() {
        return this.randomStream;
    }

    /**
     * Returns the word size of this randomiser.
     *
     * @return the word size
     */
    protected final int getWordSize() {
        return this.wordSize;
    }

    /**
     * Returns the string representation of this {@code Randomiser}
     * object
     *
     * @return the string representation of this {@code Randomiser}
     *         object
     */
    public String toString() {
        return "rst+pglj/security/Randomiser:" + this.wordSize + "$" + this.getRandomStream().toString();
    }

}
