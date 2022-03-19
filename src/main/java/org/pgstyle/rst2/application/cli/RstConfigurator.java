package org.pgstyle.rst2.application.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import org.pgstyle.rst2.application.ApplicationException;
import org.pgstyle.rst2.application.RandomStringTools;
import org.pgstyle.rst2.application.common.RstConfig;
import org.pgstyle.rst2.application.common.RstConfig.RstType;
import org.pgstyle.rst2.application.common.RstResources;
import org.pgstyle.rst2.application.common.RstUtils;

/**
 * The {@code RstConfigurator} is the controller for loading
 * {@code CommandLineArguments} and create the configuration container instance
 * of {@code RstConfig}. And the {@code RstConfigurator} also provides
 * command-line interactive configuration, which can use the command-line
 * interface interactively to allow user to configure the {@code RstConfig} with
 * guided operations.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public final class RstConfigurator {

    /**
     * Creates an {@code RstConfigurator} and loads in command-line arguments.
     *
     * @param cmdlArgs the command-line arguments container
     * @throws IllegalArgumentException
     *         if the loaded command-line arguments container contains invalid
     *         argument conbinations
     * @throws NullPointerException
     *         if the command-line arguments container is {@code null}
     */
    public RstConfigurator(CommandLineArguments cmdlArgs) {
        Objects.requireNonNull(cmdlArgs, "cmdlArgs == null");
        this.rstConfig = new RstConfig();
        try {
            this.rstConfig.type(RstType.valueOf(cmdlArgs.type()));
        }
        catch (RuntimeException e) {
            throw new IllegalArgumentException("invalid randomiser: " + cmdlArgs.type(), e);
        }
        if (RstType.ALPHANUMERIC.equals(this.rstConfig.type())) {
            this.rstConfig.ratio(Double.parseDouble(cmdlArgs.ratio()));
        }
        else if (RstType.WEIGHTED.equals(this.rstConfig.type())) {
            this.rstConfig.clear();
            String[] weights = RstUtils.safeSplit(cmdlArgs.weight(), new char[] { ';' });
            for (int i = 0; i < weights.length; i++) {
                try {
                    this.rstConfig.put(weights[i]);
                }
                catch (IllegalArgumentException e) {
                    throw new ApplicationException("syntax error in weight: " + weights[i], e);
                }
            }
        }
        this.rstConfig.length(Integer.parseInt(cmdlArgs.length()));
        this.rstConfig.output(Optional.ofNullable(cmdlArgs.output()).filter(s -> !s.isEmpty()).map(Paths::get).map(Path::toFile).orElse(null));
        this.rstConfig.secure(cmdlArgs.secure());
        this.rstConfig.seed(cmdlArgs.seed());

        // enter interactive mode
        if (cmdlArgs.interactive()) {
            this.interactive();
        }
    }

    /** The configuration container carries all configuration. */
    private RstConfig rstConfig;

    /**
     * Returns the configuration container of this configurator.
     *
     * @return the configuration container of this configurator
     */
    public RstConfig getConfig() {
        return this.rstConfig;
    }

    /**
     * Interactive controller of the guided configuring mode.
     */
    private void interactive() {
        boolean commit = false;
        RandomStringTools.head();
        while (!commit) {
            String result = null;
            boolean quit = true;
            CmdUtils.stdout(this.rstConfig.menu());
            if (RstResources.exist("rst.text.interaction")) {
                CmdUtils.stdout(RstResources.get("rst.text.interaction"));
                CmdUtils.stdout(": ");
                result = this.interaction(CmdUtils.stdin());
                quit = "i:quit".equals(result);
            }
            else {
                CmdUtils.stderr(RstResources.get("rst.text.interaction"));
                this.rstConfig.state(RandomStringTools.FAIL_DOC);
            }
            this.rstConfig.skip(quit);
            commit = quit || "i:commit".equals(result);
        }
    }

    /**
     * Controller handles user inputs.
     *
     * @param input the input text from command-line
     * @return the action summary of the controller
     */
    private String interaction(String input) {
        switch (input) {
        case "algorithm":
        case "a":
            return "i:algorithm/" + this.algorithm();
        case "length":
        case "l":
            return "i:length/" + this.length();
        case "output":
        case "o":
            return "i:output/" + this.output();
        case "d":
        case "default":
            return "i:default/" + this.reset();
        case "secure":
        case "e":
            return "i:secure/" + this.secure();
        case "seed":
        case "s":
            return "i:seed/" + this.seed();
        case "ratio":
        case "r":
            return "i:ratio/" + this.ratio();
        case "commit":
        case "c":
            return "i:commit";
        case "quit":
        case "q":
            return "i:quit";
        default:
            return "i:input/" + this.weight(input);
        }
    }

    /**
     * Controller handles interactive randomiser type selector.
     *
     * @return the action summary of the controller
     */
    private String algorithm() {
        RstType[] type = Arrays.stream(RstType.values()).filter(s -> !RstType.NUMBER.equals(s)).toArray(RstType[]::new);
        while (true) {
            StringBuilder prompt = new StringBuilder();
            IntStream.range(0, type.length).mapToObj(i -> String.format(String.format("%%%dd.%%s%%n", (int) Math.log10(type.length + 1.0) + 1), i + 1, type[i])).forEach(prompt::append);
            prompt.append(RstResources.get("rst.text.algorithm"));
            prompt.append(": ");
            CmdUtils.stdout(prompt);
            String result = CmdUtils.stdin();
            try {
                if (result.isEmpty()) {
                    CmdUtils.stdout("no change has be made" + RstUtils.NEWLINE);
                    return "cancelled";
                }
                else {
                    this.rstConfig.type(type[Integer.parseInt(result) - 1]);
                    return this.rstConfig.type().toString();
                }
            }
            catch (RuntimeException e) {
                CmdUtils.stderr("%s%nwrong selection, try again%n", RstUtils.messageOf(e));
            }
        }
    }

    /**
     * Controller handles interactive output length selector.
     *
     * @return the action summary of the controller
     */
    private String length() {
        while (true) {
            CmdUtils.stdout("Current length: %d%n", this.rstConfig.length());
            CmdUtils.stdout(RstResources.get("rst.text.length"));
            CmdUtils.stdout(": ");
            String result = CmdUtils.stdin();
            try {
                if (result.isEmpty()) {
                    CmdUtils.stdout("no change has be made" + RstUtils.NEWLINE);
                    return "cancelled";
                }
                else {
                    this.rstConfig.length(Integer.parseInt(result));
                    return String.valueOf(this.rstConfig.length());
                }
            }
            catch (RuntimeException e) {
                CmdUtils.stderr("%s%nwrong length, try again%n", RstUtils.messageOf(e));
            }
        }
    }

    /**
     * Controller handles interactive output file selector.
     *
     * @return the action summary of the controller
     */
    private String output() {
        while (true) {
            CmdUtils.stdout("Current output: %s%n", RstUtils.toQuotedString(this.rstConfig.output()));
            CmdUtils.stdout(RstResources.get("rst.text.output"));
            CmdUtils.stdout(": ");
            String result = CmdUtils.stdin();
            try {
                if (result.isEmpty()) {
                    this.rstConfig.output(null);
                    return "";
                }
                else {
                    this.rstConfig.output(Paths.get(result).toFile());
                    return String.valueOf(this.rstConfig.output());
                }
            }
            catch (RuntimeException e) {
                CmdUtils.stderr("%s%nwrong file, try again%n", RstUtils.messageOf(e));
            }
        }
    }

    /**
     * Resets all configuration stored in the {@code RstConfig} container.
     *
     * @return the action summary of the controller
     */
    private String reset() {
        this.rstConfig.reset();
        return "reset";
    }

    /**
     * Controller handles interactive alphanumeric randomiser's ratio selector.
     *
     * @return the action summary of the controller
     */
    private String ratio() {
        if (!RstType.ALPHANUMERIC.equals(this.rstConfig.type())) {
             CmdUtils.stderr("%s%nwrong input: ratio%n", RstUtils.messageOf(new IllegalStateException("not alphanumeric")));
             return "cancelled";
        }
        while (true) {
            CmdUtils.stdout("Current ratio: %.16f%n", this.rstConfig.ratio());
            CmdUtils.stdout(RstResources.get("rst.text.ratio"));
            CmdUtils.stdout(": ");
            String result = CmdUtils.stdin();
            try {
                if (result.isEmpty()) {
                    CmdUtils.stdout("no change has be made" + RstUtils.NEWLINE);
                    return "cancelled";
                }
                else {
                    this.rstConfig.ratio(Double.parseDouble(result));
                    return String.valueOf(this.rstConfig.ratio());
                }
            }
            catch (RuntimeException e) {
                CmdUtils.stderr("%s%nwrong ratio, try again%n", RstUtils.messageOf(e));
            }
        }
    }

    /**
     * Toggle the {@code Secure} flag of the {@code RstConfig} container.
     *
     * @return the action summary of the controller
     */
    private String secure() {
        this.rstConfig.secure(!this.rstConfig.secure());
        return String.valueOf(this.rstConfig.secure());
    }

    /**
     * Controller handles interactive seed input.
     *
     * @return the action summary of the controller
     */
    private String seed() {
        CmdUtils.stdout(RstResources.get("rst.text.seed"));
        String seed = CmdUtils.stdin();
        this.rstConfig.seed(seed.isEmpty() ? null : seed);
        return this.rstConfig.seed();
    }

    /**
     * Controller handles interactive weighted randomiser's weight selector.
     *
     * @param index the index of the weight descriptor to be modified
     * @return the action summary of the controller
     * @throws IllegalArgumentException
     *         if a syntax error exists in the weight statement
     * @throws NumberFormatException
     *         if wrong index is input
     */
    private String weight(int index) {
        if (index <= 0 || index > this.rstConfig.weights() + 1) {
            CmdUtils.stderr("wrong number: %d%n", index);
            throw new NumberFormatException();
        }
        CmdUtils.stdout(this.rstConfig.weights() > index - 1 ? this.rstConfig.get(index - 1) : "<new>");
        CmdUtils.stdout(RstUtils.NEWLINE);
        CmdUtils.stdout(RstResources.get("rst.text.weight"));
        CmdUtils.stdout(": ");
        String input = CmdUtils.stdin();
        if ("d".equals(input) || "delete".equals(input)) {
            return this.rstConfig.weights() > index - 1 ? "r:" + this.rstConfig.remove(index - 1) : "c:new";
        }
        if (input.isEmpty()) {
            CmdUtils.stdout("no change has be made" + RstUtils.NEWLINE);
            return "c:empty";
        }
        this.rstConfig.put(index - 1, input);
        return input;
    }

    /**
     * Controller handles interactive weighted randomiser's weight selector.
     *
     * @param input input contains the index of the weight descriptor to be
     *              modified
     * @return the action summary of the controller
     */
    private String weight(String input) {
        try {
            if (!RstType.WEIGHTED.equals(this.rstConfig.type())) {
                throw new IllegalStateException("not weighted");
            }
            int index = Integer.parseInt(input);
            return this.weight(index);
        }
        catch (NumberFormatException | IllegalStateException e) {
            CmdUtils.stderr("%s%nwrong input: %s%n", RstUtils.messageOf(e), input);
        }
        return "cancelled";
    }

}
