package org.pgstyle.rst2.application.cli;

import java.io.Console;
import java.io.PrintStream;
import java.util.Optional;

public final class CmdUtils {

    public static String stdin() {
        return Optional.ofNullable(System.console()).map(Console::readLine).orElse(null);
    }

    private static void output(PrintStream printStream, Object data) {
        printStream.print(data);
    }

    public static void stdout(Object object) {
        CmdUtils.output(System.out, object);
    }

    public static void stdout(String format, Object... objects) {
        CmdUtils.stdout(String.format(format, objects));
    }

    public static void stderr(Object object) {
        CmdUtils.output(System.err, object);
    }

    public static void stderr(String format, Object... objects) {
        CmdUtils.stderr(String.format(format, objects));
    }

    /** Unnewable @throws UnsupportedOperationException always */
    private CmdUtils() {
        throw new UnsupportedOperationException("unnewable");
    }

}
