package org.pgstyle.rst2.application.common;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import org.pgstyle.rst2.application.ApplicationException;
import org.pgstyle.rst2.random.AlphanumericRandomiser;
import org.pgstyle.rst2.random.Base64Randomiser;
import org.pgstyle.rst2.random.WeightedRandomiser;
import org.pgstyle.rst2.security.Randomiser;

/**
 * <p>
 * The {@code RandomStringGenerator} uses an {@code RstConfig} to create
 * randomiser for generating random strings.
 * </p>
 * <p>
 * Refactor of the {@code org.pgs.rst.tool.StringGenerator} class.
 * </p>
 *
 * @since rst-1
 * @version rst-2.0
 * @author PGKan
 */
public final class RandomStringGenerator {

    /**
     * Creates a {@code RandomStringGenerator} with an {@code RstConfig}
     * configuration container.
     *
     * @param rstConfig the configuration container
     * @throws ApplicationException
     *         if the configuration container contains configuration that leads
     *         to failure when creating the randomiser
     * @throws NullPointerException
     *          if the argument {@code rstConfig} is {@code null}
     */
    public RandomStringGenerator(RstConfig rstConfig) {
        Objects.requireNonNull(rstConfig, "rstConfig");
        this.rstConfig = rstConfig;
        try {
            this.randomiser = this.makeRandomiser();
        } catch (RuntimeException e) {
            throw new ApplicationException("failed to create randomiser", e);
        }
    }

    /** Randomiser configured to generate the required random string. */
    private Randomiser randomiser;
    /** Configuration container. */
    private RstConfig  rstConfig;

    /**
     * Generates a random string with the configured randomiser.
     *
     * @return a randomly generated string that matches the required
     *         specification
     */
    public String generate() {
        return new String(this.randomiser.generate(this.rstConfig.length()), StandardCharsets.UTF_8);
    }

    /**
     * Creates a randomiser with the stored {@code RstConfig} container.
     *
     * @return an instance of {@code Randomiser}
     */
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

    /**
     * Creates an alphanumeric randomiser with the stored {@code RstConfig}
     * container.
     *
     * @return an instance of {@code AlphaNumericRandomiser}
     */
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

    /**
     * Creates a base64 randomiser with the stored {@code RstConfig} container.
     *
     * @return an instance of {@code Base64Randomiser}
     */
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

    /**
     * Creates a weighted randomiser with the stored {@code RstConfig} container.
     *
     * @return an instance of {@code WeightedRandomiser}
     */
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
