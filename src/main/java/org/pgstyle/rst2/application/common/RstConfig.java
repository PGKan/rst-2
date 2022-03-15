package org.pgstyle.rst2.application.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.pgstyle.rst2.application.ApplicationException;

/**
 * The {@code RstConfig} is a container of configurations for the
 * {@code RandomStringTools} application.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public final class RstConfig {

    /**
     * The list of available randomiser types for the {@code RandomStringTools}.
     */
    public enum RstType {
        /**
         * @see org.pgstyle.rst2.random.AlphanumericRandomiser
         */
        ALPHANUMERIC,
        /**
         * @see org.pgstyle.rst2.random.Base64Randomiser
         */
        BASE64,
        /**
         * Cannot be used in the {@code RandomStringTools}.
         *
         * @see org.pgstyle.rst2.random.NumberRandomiser
         */
        NUMBER,
        /**
         * @see org.pgstyle.rst2.random.WeightedRandomiser
         */
        WEIGHTED;
    }

    /**
     * Creates a new configuration container with preloaded default
     * configurations.
     */
    public RstConfig() {
        this.weights = new ArrayList<>();
        this.reset();
    }

    /** The length of randomiser output. */
    private int     length;
    /** The ratio of numeric digits to alphabet for {@code AlphanumericRandomiser}. */
    private double  ratio;
    /** Uses secured randomiser. */
    private boolean secure;
    /** The seed for the randomiser. */
    private String  seed;
    /** The type of randomiser selected. */
    private RstType type;
    /** The weight descriptors for {@code WeightedRandomiser}. */
    private final List<String> weights;

    /** Indicates skip engaging the randomiser and end the application directly. */
    private boolean skip;
    /** The exiting state of the application. */
    private int     state;

    /**
     * Converts the weight descriptor in the representation of a map.
     *
     * @return the map representation of the weight descriptor
     * @throws ApplicationException
     *         if the weight descriptor contains syntax error
     */
    public Map<String, Integer> compile() {
        try {
            Map<String, Integer> map = new HashMap<>();
            RstUtils.dissect(this.raw()).forEach(e -> map.put(RstUtils.expand(RstUtils.normalise(e.getKey())), e.getValue()));
            for (String weight : this.weights) {
                if (weight.isEmpty()) {
                    continue;
                }
                if (!weight.contains(":")) {
                    throw new IllegalArgumentException("invalid weight descriptor: " + weight);
                }
                int value = Integer.parseInt(weight.substring(0, weight.indexOf(":")).trim());
                String statement = RstUtils.expand(RstUtils.normalise(weight.substring(weight.indexOf(":") + 1).trim()));
                if (map.containsKey(statement)) {
                    map.put(statement, map.get(statement) + value);
                }
                else {
                    map.put(statement, value);
                }
            }
            return map;
        }
        catch (RuntimeException e) {
            throw new ApplicationException("syntax error on weight", e);
        }
    }

    /**
     * Removes all weight descriptors in this container.
     */
    public void clear() {
        this.weights.clear();
    }

    /**
     * Returns the weight descriptor for {@code WeightedRandomiser} at the given
     * index.
     *
     * @param index the index of the weight descriptor
     * @return the weight descriptor for {@code WeightedRandomiser} at the given
     *         index
     * @throws IndexOutOfBoundsException
     *         if the index is out of range
     */
    public String get(int index) {
        return this.weights.get(index);
    }

    /**
     * Returns the length of randomiser output
     *
     * @return the length of randomiser output
     */
    public int length() {
        return this.length;
    }

    /**
     * Sets the length of randomiser output
     *
     * @param length the length of randomiser output
     * @throws IllegalArgumentException
     *         if the length is negative
     */
    public void length(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        this.length = length;
    }

    /**
     * Appends a weight descriptor to this configuration container.
     *
     * @param weight the weight descriptor to be stored
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws NullPointerException
     *         if the weight statement is {@code null}
     */
    public void put(String weight) {
        this.put(this.weights(), weight);
    }

    /**
     * Puts a weight descriptor at a specific index at this configuration
     * container.
     *
     * @param index the index to be stored at
     * @param weight the weight descriptor to be stored
     * @return the old weight descriptor if the specified index has already been
     *         occupied
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws IndexOutOfBoundsException
     *         if the specified index exceed the valid range
     * @throws NullPointerException
     *         if the weight statement is {@code null}
     */
    public String put(int index, String weight) {
        Objects.requireNonNull(weight, "weight == null");
        weight = weight.substring(0, weight.lastIndexOf(':') + 1) + RstUtils.normalise(weight.substring(weight.lastIndexOf(':') + 1));
        if (index == this.weights()) {
            this.weights.add(index, weight);
            return null;
        }
        return this.weights.set(index, weight);
    }

    /**
     * Returns the ratio of numeric digits to alphabet for
     * {@code AlphanumericRandomiser}.
     *
     * @return the ratio of numeric digits to alphabet for
     *         {@code AlphanumericRandomiser}
     */
    public double ratio() {
        return this.ratio;
    }

    /**
     * Sets the ratio of numeric digits to alphabet for
     * {@code AlphanumericRandomiser}.
     * @param ratio the ratio of numeric digits to alphabet for
     *              {@code AlphanumericRandomiser}
     * @throws IllegalArgumentException
     *         if the ratio exceed the valid range for
     *         {@code AlphanumericRandomiser}
     */
    public void ratio(double ratio) {
        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("ratio < 0 || ratio > 1");
        }
        this.ratio = ratio;
    }

    /**
     * Returns the raw string representation of the weight descriptors stored in
     * this configuration container.
     *
     * @return the raw string representation of the weight descriptors
     */
    public String raw() {
        return this.weights.stream().collect(Collectors.joining(";"));
    }

    /**
     * Removes a weight descriptor at the specified index of this configuration
     * container.
     *
     * @param index the index of the weight descriptor to be removed
     * @return the removed weight descriptor
     * @throws IndexOutOfBoundsException
     *         if the specified index exceed the valid range
     */
    public String remove(int index) {
        return this.weights.remove(index);
    }

    /**
     * Resets the configuration container to default configuration.
     */
    public void reset() {
        this.length = 256;
        this.ratio = 10.0 / 36;
        this.secure = false;
        this.seed = null;
        this.skip = false;
        this.state = 0;
        this.type = RstType.BASE64;
        this.weights.clear();
        this.weights.add("1:0..9a..z");
    }

    /**
     * Returns {@code true} if the secure flag is set
     *
     * @return {@code true} if the secure flag is set; or {@code false}
     * otherwise
     */
    public boolean secure() {
        return this.secure;
    }

    /**
     * Sets the secure flag of this configuration container.
     *
     * @param secure the secure flag
     */
    public void secure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Returns the seed stored in this configuration container.
     *
     * @return the seed stored in this configuration container
     */
    public String seed() {
        return Optional.ofNullable(this.seed).filter(s -> !s.isEmpty()).orElse(null);
    }

    /**
     * Sets the seed of this configuration container.
     *
     * @param seed the seed to be stored
     */
    public void seed(String seed) {
        this.seed = seed;
    }

    /**
     * Returns {@code true} if the skip flag is set in this configuration
     * container.
     *
     * @return {@code true} if the skip flag is set; or {@code false} otherwise
     */
    public boolean skip() {
        return this.skip;
    }

    /**
     * Sets the skip flag of this configuration container.
     *
     * @param skip the skip flag
     */
    public void skip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Returns the exiting state of the {@code RandomStringTools} application
     * stored at this configuration container.
     *
     * @return the exiting state of the {@code RandomStringTools}
     */
    public int state() {
        return this.state;
    }

    /**
     * Sets the exiting state of the {@code RandomStringTools} application.
     *
     * @param state the exiting state to be stored
     */
    public void state(int state) {
        this.state = state;
    }

    /**
     * Returns the type of randomiser of the {@code RandomStringTools}
     * application.
     *
     * @return the type of randomiser
     */
    public RstType type() {
        return this.type;
    }

    /**
     * Sets the type of randomiser of the {@code RandomStringTools} application.
     *
     * @param type the type of randomiser to be stored
     * @throws NullPointerException
     *         if the argument {@code type} is {@code null}
     */
    public void type(RstType type) {
        Objects.requireNonNull(type, "type == null");
        this.type = type;
    }

    /**
     * Returns the number of weight descriptors stored in this configuration
     * container.
     *
     * @return the number of weight descriptors stored in this configuration
     * container
     */
    public int weights() {
        return this.weights.size();
    }

    /**
     * Creates the menu form of this configuration container.
     *
     * @return the string formatted as a menu of this configuration container
     */
    public String menu() {
        StringBuilder string = new StringBuilder();
        int log10 = (int) (Math.log10(this.weights.size() + 1.0) + 1);
        string.append("Algorithm: ").append(this.type()).append(System.lineSeparator());
        string.append("Legnth: ").append(this.length()).append(System.lineSeparator());
        string.append("Seed: ").append(RstUtils.toQuotedString(this.seed())).append(System.lineSeparator());
        string.append("Secure: ").append(this.secure()).append(System.lineSeparator());
        if (RstType.ALPHANUMERIC.equals(this.type())) {
            string.append("Ratio: ").append(this.ratio()).append(System.lineSeparator());
        }
        if (RstType.WEIGHTED.equals(this.type())) {
            IntStream.range(0, this.weights.size()).forEach(i -> string.append(String.format(String.format("%%%dd. %%s%%n", log10), i + 1, this.weights.get(i))));
            string.append(String.format(String.format("%%%dd. <new>%%n", log10), this.weights.size() + 1));
        }
        return string.toString();
    }

}
