package org.pgstyle.rst2.main;

import org.pgstyle.rst2.application.RandomStringTools;
import org.pgstyle.rst2.application.cli.CommandLineArguments;

/**
 * Application entrypoint.
 */
public final class Main {

    /**
     * Application entrypoint.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        System.exit(new RandomStringTools(CommandLineArguments.fromArgs(args)).call());
    }
}
