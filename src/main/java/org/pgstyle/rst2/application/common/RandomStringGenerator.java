package org.pgstyle.rst2.application.common;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.pgstyle.rst2.application.ApplicationException;
import org.pgstyle.rst2.random.AlphanumericRandomiser;
import org.pgstyle.rst2.random.Base64Randomiser;
import org.pgstyle.rst2.random.WeightedRandomiser;
import org.pgstyle.rst2.security.Randomiser;

public final class RandomStringGenerator {

    public RandomStringGenerator(RstConfig rstConfig) {
        Objects.requireNonNull(rstConfig, "rstConfig");
        this.rstConfig = rstConfig;
        try {
            this.randomiser = this.makeRandomiser();
        } catch (RuntimeException e) {
            throw new ApplicationException("failed to create randomiser", e);
        }
    }

    private Randomiser randomiser;
    private RstConfig  rstConfig;

    public String generate() {
        return new String(this.randomiser.generate(this.rstConfig.length()), StandardCharsets.UTF_8);
    }

    private Randomiser makeRandomiser() {
        switch (this.rstConfig.type()) {
            case ALPHANUMERIC:
                return this.makeAlphanumeric();
            case BASE64:
                return this.makeBase64();
            case WEIGHTED:
                return this.makeWeighted();
            case NUMBER:
            default:
                // for the use of RandomStringTools as an application,
                // only Alphanumeric, Base64 and Weighted are allowed
                throw new IllegalArgumentException("invalid randomiser type: " + this.rstConfig.type().name());
        }
    }

    private AlphanumericRandomiser makeAlphanumeric() {
        String seed = this.rstConfig.seed();
        if (this.rstConfig.secure()) {
            return Objects.isNull(seed) ? AlphanumericRandomiser.getInstanceSecure(this.rstConfig.ratio())
                                        : AlphanumericRandomiser.getInstanceSecure(this.rstConfig.ratio(), seed);
        }
        else {
            return Objects.isNull(seed) ? AlphanumericRandomiser.getInstance(this.rstConfig.ratio())
                                        : AlphanumericRandomiser.getInstance(this.rstConfig.ratio(), seed);
        }
    }

    private Base64Randomiser makeBase64() {
        String seed = this.rstConfig.seed();
        if (this.rstConfig.secure()) {
            return Objects.isNull(seed) ? Base64Randomiser.getInstanceSecure()
                                        : Base64Randomiser.getInstanceSecure(seed);
        }
        else {
            return Objects.isNull(seed) ? Base64Randomiser.getInstance()
                                        : Base64Randomiser.getInstance(seed);
        }
    }

    private WeightedRandomiser makeWeighted() {
        String seed = this.rstConfig.seed();
        Map<String, Integer> weight = this.rstConfig.compile();
        if (this.rstConfig.secure()) {
            return Objects.isNull(seed) ? WeightedRandomiser.getInstanceSecure(weight)
                                        : WeightedRandomiser.getInstanceSecure(weight, seed);
        }
        else {
            return Objects.isNull(seed) ? WeightedRandomiser.getInstance(weight)
                                        : WeightedRandomiser.getInstance(weight, seed);
        }
    }


}
