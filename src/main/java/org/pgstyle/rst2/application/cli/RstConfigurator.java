package org.pgstyle.rst2.application.cli;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

import org.pgstyle.rst2.application.ApplicationException;
import org.pgstyle.rst2.application.RandomStringTools;
import org.pgstyle.rst2.application.common.RstConfig;
import org.pgstyle.rst2.application.common.RstConfig.RstType;
import org.pgstyle.rst2.application.common.RstResources;
import org.pgstyle.rst2.application.common.RstUtils;

public final class RstConfigurator {

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
        this.rstConfig.secure(cmdlArgs.secure());
        this.rstConfig.seed(cmdlArgs.seed());

        // enter interactive mode
        if (cmdlArgs.interactive()) {
            this.interactive();
        }
    }

    private RstConfig rstConfig;

    public RstConfig getConfig() {
        return this.rstConfig;
    }

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

    private String interaction(String input) {
        switch (input) {
        case "algorithm":
        case "a":
            return "i:algorithm/" + this.algorithm();
        case "length":
        case "l":
            return "i:length/" + this.length();
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

    private String reset() {
        this.rstConfig.reset();
        return "reset";
    }

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

    private String secure() {
        this.rstConfig.secure(!this.rstConfig.secure());
        return String.valueOf(this.rstConfig.secure());
    }

    private String seed() {
        CmdUtils.stdout(RstResources.get("rst.text.seed"));
        String seed = CmdUtils.stdin();
        this.rstConfig.seed(seed.isEmpty() ? null : seed);
        return this.rstConfig.seed();
    }

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
