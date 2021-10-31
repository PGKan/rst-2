package org.pgstyle.rst2.application;

import java.util.concurrent.Callable;

import org.pgstyle.rst2.application.cli.CmdUtils;
import org.pgstyle.rst2.application.cli.CommandLineArguments;
import org.pgstyle.rst2.application.cli.RstConfigurator;
import org.pgstyle.rst2.application.common.RandomStringGenerator;
import org.pgstyle.rst2.application.common.RstConfig;
import org.pgstyle.rst2.application.common.RstResources;
import org.pgstyle.rst2.application.common.RstUtils;
import org.pgstyle.rst2.application.gui.RstMainFrame;

/**
 * This class is the main logic controller of the {@code RandomStringTools}
 * application.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public final class RandomStringTools implements Callable<Integer> {

    /** Exiting state: 0 Success */
    public static final int SUCCESS   = 0;
    /** Exiting state: 255 Document Failure */
    public static final int FAIL_DOC  = 255;
    /** Exiting state: 1 Argument Failure */
    public static final int FAIL_ARG  = 1;
    /** Exiting state: 2 Initialisation Failure */
    public static final int FAIL_INIT = 2;
    /** Exiting state: 4 Interrupted */
    public static final int FAIL_INTR = 4;

    /**
     * Creates an application controller with loaded command-line arguments.
     *
     * @param cmdlArgs the loaded command-line arguments
     */
    public RandomStringTools(CommandLineArguments cmdlArgs) {
        this.cmdlArgs = cmdlArgs;
    }

    /** Command-line argument storage */
    private final CommandLineArguments cmdlArgs;

    /**
     * Use the application controller.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    @Override
    public Integer call() {
        // standard command line support -h/-v
        if (this.cmdlArgs.help()) {
            return RandomStringTools.help();
        }
        if (this.cmdlArgs.version()) {
            return RandomStringTools.version();
        }
        RstConfig config = new RstConfigurator(this.cmdlArgs).getConfig();
        if (this.cmdlArgs.gui()) {
            // start GUI mode
            CmdUtils.stdout("Start in GUI mode..." + RstUtils.NEWLINE);
            RstMainFrame frame = new RstMainFrame(config);
            int code = RandomStringTools.SUCCESS;
            try {
                synchronized (this) {
                    while (this.cmdlArgs.gui()) {
                        this.wait();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (frame.isClosed()) {
                    code = RandomStringTools.SUCCESS;
                }
                else {
                    code = RandomStringTools.FAIL_INTR;
                }
            }
            CmdUtils.stdout("GUI mode exited with code %d%n", code);
            return code;
        }
        else {
            // start CLI mode
            return RandomStringTools.main(config);
        }
    }

    /**
     * CLI mode entrypoint of the {@code RandomStringTools}.
     *
     * @param rstConfig the configuration container loaded from command-line
     *                  arguments
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    private static int main(RstConfig rstConfig) {
        if (rstConfig.skip()) {
            return RandomStringTools.SUCCESS;
        }
        try {
            RandomStringGenerator rsg = new RandomStringGenerator(rstConfig);
            CmdUtils.stdout("%s%n", rsg.generate());
        }
        catch (RuntimeException e) {
            CmdUtils.stderr("failed to engage randomiser%n%s", RstUtils.stackTraceOf(e));
            return RandomStringTools.FAIL_INIT;
        }
        return RandomStringTools.SUCCESS;
    }

    /**
     * Prints the header text.
     */
    public static void head() {
        RandomStringTools.printResourceText("rst.text.head");
    }

    /**
     * Prints the help text.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    public static int help() {
        return RandomStringTools.printResourceText("rst.text.help");
    }

    /**
     * Prints the version info text.
     *
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    public static int version() {
        return RandomStringTools.printResourceText("rst.text.version");
    }

    /**
     * Prints the text in a resource file.
     *
     * @param key the key of the resource
     * @return the exiting state, a non-zero state indicate error has occurred
     */
    private static int printResourceText(String key) {
        if (RstResources.exist(key)) {
            CmdUtils.stdout(RstResources.get(key));
            return RandomStringTools.SUCCESS;
        }
        else {
            CmdUtils.stderr(RstResources.get(key));
            return RandomStringTools.FAIL_DOC;
        }
    }

}
