package org.pgstyle.rst2.application.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import org.pgstyle.rst2.application.cli.CmdUtils;
import org.pgstyle.rst2.application.common.RandomStringGenerator;
import org.pgstyle.rst2.application.common.RstConfig;
import org.pgstyle.rst2.application.common.RstConfig.RstType;
import org.pgstyle.rst2.application.common.RstResources;
import org.pgstyle.rst2.application.common.RstUtils;

/**
 * <p>
 * Frame controller of graphical interface of the {@code RandomStringTools}.
 * </p>
 * <p>
 * Refactor of the {@code org.pgs.rst.windows.MainWindow} class.
 * </p>
 *
 * @since rst-1
 * @version rst-2.0
 * @author PGKan
 */
public final class RstMainFrame {

    public static final Font MONO;
    public static final Font MONOBOLD;
    public static final Image ICON;

    static {
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, RstResources.getStream("rst.font.mono")).deriveFont(Font.PLAIN, 13);
        } catch (FontFormatException | IOException e) {
            CmdUtils.stderr(RstUtils.stackTraceOf(e));
        }
        MONO = font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, RstResources.getStream("rst.font.monobold")).deriveFont(Font.PLAIN, 13);
        } catch (FontFormatException | IOException e) {
            CmdUtils.stderr(RstUtils.stackTraceOf(e));
        }
        MONOBOLD = font;
        Image icon = null;
        try {
            icon = ImageIO.read(RstResources.getStream("rst.icon.rst2"));
        } catch (IOException | RuntimeException e) {
            CmdUtils.stderr(RstUtils.stackTraceOf(e));
        }
        ICON = icon;
    }

    /**
     * Initialises the main window of the {@code RandomStringTools}
     * @param config the configuration loaded from the command line
     */
    public RstMainFrame(RstConfig config) {
        this.rstConfig = config;
        // setup the GUI appearance
        this.frame = new JFrame("RST2 - Random String Tools Gen 2");
        this.frame.setSize(640, 480);
        this.frame.setResizable(false);
        this.frame.setLocationRelativeTo(null);
        this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.frame.getContentPane().setLayout(new BorderLayout());
        this.frame.setIconImage(RstMainFrame.ICON);

        // setup close events
        Thread main = Thread.currentThread();
        this.frame.addWindowListener(new WindowAdapter() {
            @Override
            public synchronized void windowClosing(WindowEvent e) {
                RstMainFrame.this.closed = true;
                main.interrupt();
            }
        });

        // create subelements
        this.weightsConfig = new RstWeightsFrame(this, this.frame);
        this.frame.add(this.makeConfigPanel(), BorderLayout.NORTH);
        this.frame.add(this.makeOutputPanel(), BorderLayout.CENTER);

        // load setting before showing the window
        this.loadDefault(this.rstConfig);
        this.frame.setVisible(true);
    }

    /** Reference to the controller of the weight descriptor configuration dialog. */
    private RstWeightsFrame weightsConfig;
    /** Stored configurations. */
    private RstConfig rstConfig;
    /** Closing state of the main window. */
    private boolean closed;

    /** Reference to the main window. */
    private JFrame frame;
    /** Text area for outputting generated string or program messages. */
    private JTextArea output;
    /** The generator type selector. */
    private JComboBox<RstType> algorithm;
    /** The ratio selector of the {@code AlphanumericRandomiser}. */
    private JSpinner ratio;
    /** The input field of {@code WeightedRandomiser}. */
    private JTextField weights;
    /** The secure selector of all types of randomiser. */
    private JCheckBox secure;
    /** The seed input of all types of randomiser. */
    private JTextField seed;
    /** The generating length input of all types of randomiser. */
    private JSpinner length;

    /**
     * Returns {@code true} if the main window is closed.
     *
     * @return {@code true} if the main window is closed; of {@code false}
     *         otherwise
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Resets the output text field of the main window and prints string onto it.
     *
     * @param string the string to be printed
     */
    public void rewrite(String string) {
        this.output.setText("");
        this.write(string);
    }

    /**
     * Prints string onto the output text field of the main window.
     *
     * @param string the string to be printed
     */
    public void write(String string) {
        this.output.append(string);
        this.output.append(System.lineSeparator());
        this.output.setCaretPosition(this.output.getDocument().getLength());
    }

    /**
     * Updates the weight descriptor with the given string.
     *
     * @param weights the new weights descriptor
     */
    public void updateWeights(String weights) {
        this.weights.setText(weights);
    }

    /**
     * Creates the subelements for configuring the randomiser.
     *
     * @return a {@code JPanel} containing the subelements
     */
    private JPanel makeConfigPanel() {
        // setup config panel
        FlowLayout defaultLayout = new FlowLayout(FlowLayout.LEADING, 5, 0);
        JPanel config = new JPanel();
        GroupLayout layout = new GroupLayout(config);
        config.setLayout(layout);

        // layer 1
        JPanel layer1 = new JPanel(defaultLayout);
        // randomiser type selector
        JLabel algorithmLabel = new JLabel("algorithm =");
        algorithmLabel.setFont(RstMainFrame.MONOBOLD);
        this.algorithm = new JComboBox<>();
        algorithm.setFont(RstMainFrame.MONOBOLD);
        this.algorithm.addItem(RstType.ALPHANUMERIC);
        this.algorithm.addItem(RstType.BASE64);
        this.algorithm.addItem(RstType.WEIGHTED);
        // secure selector
        this.secure = new JCheckBox("useSecureInstance");
        secure.setFont(RstMainFrame.MONOBOLD);
        layer1.add(algorithmLabel);
        layer1.add(this.algorithm);
        layer1.add(this.secure);

        // layer 2 only alphanumeric or weighted
        JPanel layer2 = new JPanel(defaultLayout);
        // ratio of latin alphabets and numeric digits for alphanumeric randomiser
        JLabel ratioLabel = new JLabel("ratio =");
        ratioLabel.setFont(RstMainFrame.MONOBOLD);
        this.ratio = new JSpinner(new SpinnerNumberModel(0, 0, 1, 0.001));
        this.ratio.setFont(RstMainFrame.MONO);
        ((JSpinner.NumberEditor) this.ratio.getEditor()).getTextField().setColumns(9);
        ((JSpinner.NumberEditor) this.ratio.getEditor()).getFormat().setMinimumFractionDigits(7);
        // weight descriptor for weighted randomiser
        JLabel weightsLabel = new JLabel("weights =");
        weightsLabel.setFont(RstMainFrame.MONOBOLD);
        this.weights = new JTextField();
        weights.setFont(RstMainFrame.MONO);
        weights.setColumns(56);
        // button for opening the configuration dialog for weight descriptor
        JButton weightsButton = new JButton("Config");
        weightsButton.setFont(RstMainFrame.MONOBOLD);
        weightsButton.addActionListener(e -> this.weightsConfig.config(this.weights.getText()));
        layer2.add(ratioLabel);
        layer2.add(this.ratio);
        layer2.add(weightsLabel);
        layer2.add(this.weights);
        layer2.add(weightsButton);
        // update the visibility of layer 2 when the randomiser type changed
        this.algorithm.addActionListener(e -> {
            RstType type = this.algorithm.getItemAt(this.algorithm.getSelectedIndex());
            switch (type) {
                case ALPHANUMERIC:
                    layer2.setVisible(true);
                    ratioLabel.setVisible(true);
                    this.ratio.setVisible(true);
                    weightsLabel.setVisible(false);
                    this.weights.setVisible(false);
                    weightsButton.setVisible(false);
                    break;
                case BASE64:
                    layer2.setVisible(false);
                    break;
                case WEIGHTED:
                    layer2.setVisible(true);
                    ratioLabel.setVisible(false);
                    this.ratio.setVisible(false);
                    weightsLabel.setVisible(true);
                    this.weights.setVisible(true);
                    weightsButton.setVisible(true);
                    break;
                default:
                    this.write("Error: wrong RST type: " + type.name());
                    break;
                }
        });

        // layer 3
        JPanel layer3 = new JPanel(defaultLayout);
        JLabel seedLabel = new JLabel("seed =");
        seedLabel.setFont(RstMainFrame.MONOBOLD);
        this.seed = new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                // create placeholder text
                if (this.getText().isEmpty()) {
                    ((Graphics2D) graphics).setColor(this.getDisabledTextColor());
                    ((Graphics2D) graphics).drawString("(leave empty to use auto-generated seed)",
                                                       this.getInsets().left,
                                                       graphics.getFontMetrics().getMaxAscent() + this.getInsets().top);
                }
            }
        };
        this.seed.setFont(RstMainFrame.MONO);
        this.seed.setColumns(70);
        layer3.add(seedLabel);
        layer3.add(this.seed);

        // layer 4
        JPanel layer4 = new JPanel(defaultLayout);
        JLabel lengthLabel = new JLabel("length =");
        lengthLabel.setFont(RstMainFrame.MONOBOLD);
        this.length = new JSpinner(new SpinnerNumberModel(0, 0, 65536, 1));
        length.setFont(RstMainFrame.MONO);
        JButton generate = new JButton("Generate");
        generate.setFont(RstMainFrame.MONOBOLD);
        generate.addActionListener(e -> this.commit());
        layer4.add(lengthLabel);
        layer4.add(this.length);
        layer4.add(generate);

        // layout settings
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
                  .addComponent(layer1)
                  .addComponent(layer2)
                  .addComponent(layer3)
                  .addComponent(layer4)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .addGap(5)
                  .addComponent(layer1)
                  .addComponent(layer2)
                  .addComponent(layer3)
                  .addComponent(layer4)
        );
        return config;
    }

    /**
     * Creates the subelements for output box.
     *
     * @return a {@code JPanel} containing the subelements
     */
    private JPanel makeOutputPanel() {
        JPanel outputPanel = new JPanel();
        GroupLayout outputLayout = new GroupLayout(outputPanel);
        outputPanel.setLayout(outputLayout);

        // subelements
        JLabel outputLabel = new JLabel("output:");
        outputLabel.setFont(RstMainFrame.MONOBOLD);
        JButton copy = new JButton("Copy");
        copy.setFont(RstMainFrame.MONOBOLD);
        copy.addActionListener(e -> this.copy());
        this.output = new JTextArea("");
        JScrollPane outputPane = new JScrollPane(this.output,
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        this.output.setFont(MONO);
        this.output.setLineWrap(true);
        this.output.setEditable(false);
        this.output.setBorder(BorderFactory.createEtchedBorder());

        // setup layout
        outputLayout.setHorizontalGroup(
            outputLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(
                            outputLayout.createSequentialGroup()
                                        .addGap(5)
                                        .addComponent(outputLabel)
                                        .addGap(5)
                                        .addComponent(copy)
                        )
                        .addGroup(
                            outputLayout.createSequentialGroup()
                                        .addGap(5)
                                        .addComponent(outputPane)
                                        .addGap(5)
                        )
        );
        outputLayout.setVerticalGroup(
            outputLayout.createSequentialGroup()
                        .addGroup(
                            outputLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(outputLabel)
                                        .addComponent(copy)
                        )
                        .addGroup(
                            outputLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(outputPane)
                        )
                        .addGap(5)
        );
        return outputPanel;
    }

    /**
     * Loads the configuration from command line into the main window.
     *
     * @param config the configuration container
     */
    private void loadDefault(RstConfig config) {
        this.algorithm.setSelectedItem(config.type());
        this.secure.setSelected(config.secure());
        this.ratio.setValue(config.ratio());
        this.weights.setText(config.raw());
        this.seed.setText(config.seed());
        this.length.setValue(config.length());
        // print to output text area
        this.write("loaded settings from RstConfig/CommandLineArguments");
        this.write("algorithm = " + config.type());
        this.write("secure = " + config.secure());
        this.write("seed = " + config.seed());
        this.write("length = " + config.length());
        if (config.type().equals(RstType.ALPHANUMERIC)) {
            this.write("ratio = " + config.ratio());
        }
        if (config.type().equals(RstType.WEIGHTED)) {
            this.write("weights = " + config.raw());
        }
    }

    /**
     * Commits all configurations and engages the randomiser.
     */
    private void commit() {
        try {
            // load configuration from GUI configurator
            RstConfig current = new RstConfig();
            current.type(this.algorithm.getItemAt(this.algorithm.getSelectedIndex()));
            current.secure(this.secure.isSelected());
            current.ratio(Double.parseDouble(((JSpinner.NumberEditor) this.ratio.getEditor()).getTextField().getText()));
            current.clear();
            for (String weight : RstUtils.safeSplit(this.weights.getText(), new char[] {';'})) {
                current.put(weight);
            }
            current.seed(this.seed.getText());
            current.length(Integer.parseInt(((JSpinner.NumberEditor) this.length.getEditor()).getTextField().getText().replace(",", "")));
            // create randomiser with configuration and generate result
            this.rewrite(new RandomStringGenerator(current).generate());
        }
        catch (RuntimeException e) {
            this.rewrite(RstUtils.stackTraceOf(e));
        }
    }

    /**
     * Copies text from the output text area to the system clipboard.
     */
    private void copy() {
        this.output.select(0, Integer.MAX_VALUE);
        this.output.requestFocus();
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.output.getText()), null);
    }

}
