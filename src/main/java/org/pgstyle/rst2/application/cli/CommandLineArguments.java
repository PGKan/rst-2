package org.pgstyle.rst2.application.cli;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pgstyle.rst2.application.common.RstConfig.RstType;
import org.pgstyle.rst2.application.ApplicationException;
import org.pgstyle.rst2.application.common.RstUtils;

public final class CommandLineArguments {

    private static final Map<String, String> cmdlArgs;
    private static final Set<String>         pFlags;

    static {
        cmdlArgs = new HashMap<>(17);
        // put named argument here
        cmdlArgs.put("-h", "Help");
        cmdlArgs.put("--help", "Help");
        cmdlArgs.put("-i", "Interactive");
        cmdlArgs.put("--interactive", "Interactive");
        cmdlArgs.put("--gui", "GUI");
        cmdlArgs.put("-l", "Length");
        cmdlArgs.put("--length", "Length");
        cmdlArgs.put("-w", "Weight");
        cmdlArgs.put("--weight", "Weight");
        cmdlArgs.put("-S", "Secure");
        cmdlArgs.put("--secure", "Secure");
        cmdlArgs.put("-s", "Seed");
        cmdlArgs.put("--seed", "Seed");
        cmdlArgs.put("-t", "Type");
        cmdlArgs.put("--type", "Type");
        cmdlArgs.put("-v", "Version");
        cmdlArgs.put("--version", "Version");
        pFlags = new HashSet<>(2);
        pFlags.add("Help");
        pFlags.add("Version");
    }

    public static CommandLineArguments fromArgs(String[] args){
        try {
        return new CommandLineArguments(CommandLineArguments.processArguments(args));
        }
        catch (RuntimeException e) {
            throw new ApplicationException("failed to process arguments", e);
        }
    }

    private static boolean priorityFlags(Set<String> keys) {
        return CommandLineArguments.pFlags.stream().anyMatch(keys::contains);
    }

    private static Map<String, String> defaultArguments() {
        Map<String, String> argMap = new HashMap<>();
        // put default arguments here
        argMap.put("Length", "256");
        argMap.put("Ratio", Double.toString(10.0 / 36));
        argMap.put("Type", "BASE64");
        argMap.put("Weight", "1:0..9a..z");
        return argMap;
    }

    private static Map<String, String> processArguments(String[] args) {
        return CommandLineArguments.processArguments(Arrays.stream(args).collect(Collectors.toList()).iterator());
    }

    private static Map<String, String> processArguments(Iterator<String> args) {
        Map<String, String> argMap = CommandLineArguments.defaultArguments();
        int position = 0;
        while (args.hasNext() && !CommandLineArguments.priorityFlags(argMap.keySet())) {
            String arg = args.next();
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("-")) {
                try {
                    CommandLineArguments.setNamedArgument(arg, argMap, args);
                }
                catch (NoSuchElementException e) {
                    throw new IllegalArgumentException(String.format("expecting argument for key \"%s\", but found none", arg), e);
                }
            }
            else {
                CommandLineArguments.setPositionArgument(position, arg, argMap, args);
                position++;
            }
        }
        return argMap;
    }

    private static void setNamedArgument(String arg, Map<String, String> map, Iterator<String> args) {
        String name = CommandLineArguments.getFlagName(arg);
        switch (name){
        // flags
        case "Help":
        case "Version":
        case "Interactive":
        case "Secure":
        case "GUI":
            map.put(name, name);
            break;
        // arguments
        case "Length":
            try {
                map.put(name, String.valueOf(Integer.parseInt(args.next())));
            }
            catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("expecting integer argument for key \"%s\", but found other", arg), e);
            }
            break;
        case "Type":
            try {
                map.put(name, RstType.valueOf(args.next()).name());
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("invalid type for argument \"%s\"", arg), e);
            }
            break;
        case "Seed":
        case "Weight":
            map.put(name, args.next());
            break;
        case "":
            throw new IllegalArgumentException("unknown argument key: " + arg);
        default:
            throw new IllegalArgumentException("unknown argument: " + name);
        }
    }

    private static void setPositionArgument(int position, String arg, Map<String, String> map, Iterator<String> args){
        switch (position) {
        // keep switch clause in case of future addition of positional argument
        case 0:
            if ("ALPHANUMERIC".equals(map.get("Type"))) {
                try {
                    map.put("Ratio", String.valueOf(Double.parseDouble(arg)));
                }
                catch (NumberFormatException e) {
                    throw new IllegalArgumentException(String.format("expecting double argument for positional argument #%d, but found other", position), e);
                }
                break;
            }
            else if ("WEIGHTED".equals(map.get("Type"))) {
                map.put("Weight", arg);
                break;
            }
        default:
            throw new IllegalArgumentException("two many arguments: " + position);
        }
    }

    private static String getFlagName(String key) {
        return Optional.ofNullable(CommandLineArguments.cmdlArgs.get(key)).orElse("");
    }

    public CommandLineArguments(Map<String, String> arguments) {
        Objects.requireNonNull(arguments, "arguments == null");
        this.arguments = new HashMap<>();
        this.arguments.putAll(arguments);
        if (!RstUtils.sysin()) {
            // no system in available, forced gui mode and disable interactive
            this.arguments.putIfAbsent("GUI", "GUI");
            this.arguments.remove("Interactive");
        }
    }

    private Map<String, String> arguments;

    public boolean isFlagSet(String key) {
        return arguments.containsKey(key);
    }

    public boolean help() {
        return this.isFlagSet("Help");
    }

    public boolean interactive() {
        return !this.gui() && this.isFlagSet("Interactive");
    }

    public boolean gui() {
        return this.isFlagSet("GUI");
    }

    public String length() {
        return Optional.ofNullable(this.arguments.get("Length")).orElse("");
    }

    public String ratio() {
        return Optional.ofNullable(this.arguments.get("Ratio")).orElse("");
    }

    public String weight() {
        return Optional.ofNullable(this.arguments.get("Weight")).orElse("");
    }

    public boolean secure() {
        return this.isFlagSet("Secure");
    }

    public String seed() {
        return this.arguments.get("Seed");
    }

    public String type() {
        return Optional.ofNullable(this.arguments.get("Type")).orElse("BASE64");
    }

    public boolean version() {
        return this.isFlagSet("Version");
    }

}
