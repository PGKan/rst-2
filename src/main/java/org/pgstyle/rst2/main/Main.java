package org.pgstyle.rst2.main;

import org.pgstyle.rst2.application.RandomStringTools;
import org.pgstyle.rst2.application.cli.CommandLineArguments;

public final class Main {

    public static void main(String[] args) {
        System.exit(new RandomStringTools(CommandLineArguments.fromArgs(args)).call());
    }
}
