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

public final class RstConfig {

    public enum RstType {
        ALPHANUMERIC,
        BASE64,
        NUMBER,
        WEIGHTED;
    }

    public RstConfig() {
        this.weights = new ArrayList<>();
        this.reset();
    }

    private int     length;
    private double  ratio;
    private boolean secure;
    private String  seed;
    private RstType type;
    private final List<String> weights;

    private boolean skip;
    private int     state;

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

    public void clear() {
        this.weights.clear();
    }

    public String get(int index) {
        return this.weights.get(index);
    }

    public int length() {
        return this.length;
    }

    public void length(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length < 0");
        }
        this.length = length;
    }

    public void put(String weight) {
        this.put(this.weights(), weight);
    }

    public String put(int index, String weight) {
        Objects.requireNonNull(weight, "weight == null");
        weight = weight.substring(0, weight.lastIndexOf(':') + 1) + RstUtils.normalise(weight.substring(weight.lastIndexOf(':') + 1));
        if (index == this.weights()) {
            this.weights.add(index, weight);
            return null;
        }
        return this.weights.set(index, weight);
    }

    public double ratio() {
        return this.ratio;
    }

    public void ratio(double ratio) {
        if (ratio < 0 || ratio > 1) {
            throw new IllegalArgumentException("ratio < 0 || ratio > 1");
        }
        this.ratio = ratio;
    }

    public String raw() {
        return this.weights.stream().collect(Collectors.joining(";"));
    }

    public String remove(int index) {
        return this.weights.remove(index);
    }

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

    public boolean secure() {
        return this.secure;
    }

    public void secure(boolean secure) {
        this.secure = secure;
    }

    public String seed() {
        return Optional.ofNullable(this.seed).filter(s -> !s.isEmpty()).orElse(null);
    }

    public void seed(String seed) {
        this.seed = seed;
    }

    public boolean skip() {
        return this.skip;
    }

    public void skip(boolean skip) {
        this.skip = skip;
    }

    public int state() {
        return this.state;
    }

    public void state(int state) {
        this.state = state;
    }

    public RstType type() {
        return this.type;
    }

    public void type(RstType type) {
        Objects.requireNonNull(type, "type == null");
        this.type = type;
    }

    public int weights() {
        return this.weights.size();
    }

    public String menu() {
        StringBuilder string = new StringBuilder();
        int log10 = (int) (Math.log10(this.weights.size() + 1.0) + 1);
        string.append("Algorithm: ").append(this.type()).append(System.lineSeparator());
        string.append("Legnth: ").append(this.length()).append(System.lineSeparator());
        string.append("Seed: ").append(Objects.isNull(this.seed()) ? "<null>" : this.seed()).append(System.lineSeparator());
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
