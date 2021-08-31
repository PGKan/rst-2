package org.pgstyle.rst2.random;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.pgstyle.rst2.security.RandomInputStream;
import org.pgstyle.rst2.security.Randomiser;
import org.pgstyle.rst2.security.SecureRandomInputStream;

public final class WeightedRandomiser extends Randomiser {

    public static WeightedRandomiser getInstance(Map<String, Integer> weights) {
        return WeightedRandomiser.getInstance(weights, RandomUtils.randomLongSeed());
    }

    public static WeightedRandomiser getInstance(Map<String, Integer> weights, long seed) {
        return new WeightedRandomiser(weights, new RandomInputStream(seed));
    }

    public static WeightedRandomiser getInstance(Map<String, Integer> weights, String seed) {
        return WeightedRandomiser.getInstance(weights, RandomUtils.toLongSeed(seed));
    }

    public static WeightedRandomiser getInstanceSecure(Map<String, Integer> weights) {
        return WeightedRandomiser.getInstanceSecure(weights, RandomUtils.randomBytesSeed());
    }

    public static WeightedRandomiser getInstanceSecure(Map<String, Integer> weights, byte[] seed) {
        return new WeightedRandomiser(weights, new SecureRandomInputStream(seed));
    }

    @Deprecated
    public static WeightedRandomiser getInstanceSecure(Map<String, Integer> weights, long seed) {
        return WeightedRandomiser.getInstanceSecure(weights, RandomUtils.toBytesSeed(seed));
    }

    public static WeightedRandomiser getInstanceSecure(Map<String, Integer> weights, String seed) {
        return WeightedRandomiser.getInstanceSecure(weights, RandomUtils.toBytesSeed(seed));
    }

    private static Map<Integer, Character> compile(Map<String, Integer> weights) {
        Objects.requireNonNull(weights, "weights == null");
        if (weights.isEmpty()) {
            throw new IllegalArgumentException("empty weight");
        }
        Map<Integer, Character> compiled = new LinkedHashMap<>();
        int current = 0;
        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            for (char c : entry.getKey().toCharArray()) {
                if (entry.getValue() == 0) {
                    continue;
                }
                try {
                    current = StrictMath.addExact(current, entry.getValue());
                }
                catch (ArithmeticException e) {
                    throw new IllegalArgumentException("weight exceeds capability of randomiser", e);
                }
                compiled.put(current, c);
            }
        }
        return compiled;
    }

    private static String summarise(Map<Integer, Character> weights) {
        Set<Character> compiled = new TreeSet<>();
        weights.forEach((i, c) -> compiled.add(c));
        return compiled.stream().map(String::valueOf).collect(Collectors.joining());
    }

    private static int total(Map<Integer, Character> weights) {
        return weights.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public WeightedRandomiser(Map<String, Integer> weights, RandomInputStream randomStream) {
        super(randomStream);
        this.weights = WeightedRandomiser.compile(weights);
        this.summary = WeightedRandomiser.summarise(this.weights);
        this.total = WeightedRandomiser.total(this.weights);

    }

    private final Map<Integer, Character> weights;
    private final String                  summary;
    private final int                     total;

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ this.weights.hashCode();
    }

    @Override
    public byte[] generate(int length) {
        return this.generateString(length).getBytes(StandardCharsets.UTF_8);
    }

    public String generateString(int length) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < length; i++) {
            string.append(this.getCharacter(this.getPointer()));
        }
        return string.toString();
    }

    private char getCharacter(int pointer) {
        for (Map.Entry<Integer, Character> entry : this.weights.entrySet()) {
            if (entry.getKey() > pointer) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("pointer missed: " + pointer);
    }

    private int getPointer() {
        double d = (ByteBuffer.wrap(super.generate(8)).getLong() >>> 11) / (double) (1l << 53);
        return (int) StrictMath.floor(d * this.total);
    }

    @Override
    public String toString() {
        return String.format("rst/WeightedRandomiser:%s#%d$%s", this.summary, this.total, this.getRandomStream());
    }

}
