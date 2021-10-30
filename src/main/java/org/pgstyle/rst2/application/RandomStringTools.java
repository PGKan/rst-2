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

public final class RandomStringTools implements Callable<Integer> {

    public static final int SUCCESS   = 0;
    public static final int FAIL_DOC  = 255;
    public static final int FAIL_ARG  = 1;
    public static final int FAIL_INIT = 2;
    public static final int FAIL_INTR = 4;

    public RandomStringTools(CommandLineArguments cmdlArgs) {
        this.cmdlArgs = cmdlArgs;
    }

    private final CommandLineArguments cmdlArgs;

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

    public static void head() {
        RandomStringTools.printResourceText("rst.text.head");
    }

    public static int help() {
        return RandomStringTools.printResourceText("rst.text.help");
    }

    public static int version() {
        return RandomStringTools.printResourceText("rst.text.version");
    }

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
