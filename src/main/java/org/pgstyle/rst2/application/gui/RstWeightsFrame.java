package org.pgstyle.rst2.application.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.pgstyle.rst2.application.common.RstUtils;

/**
 * Frame controller for the config function of weight descriptors of the
 * {@link org.pgstyle.rst2.random.WeightedRandomiser WeightedRandomiser}.
 *
 * @since rst-2
 * @version rst-2.0
 * @author PGKan
 */
public class RstWeightsFrame {

    /**
     * Weight descriptor container and controller. It is a subelement of a
     * {@code RstWeightsFrame}. It contains an action button for adding and
     * removing cells and the input area for the weight and statement.
     */
    private class RstWeightCell extends JPanel {

        /**
         * Event handler of the action button of a {@code RstWeightCell}.
         */
        private class CellOperation implements ActionListener {

            @Override
            public void actionPerformed(ActionEvent e) {
                switch (RstWeightCell.this.action.getText()) {
                case RstWeightsFrame.REMOVE:
                    RstWeightsFrame.this.descriptors.remove(RstWeightCell.this);
                    break;
                case RstWeightsFrame.ADD:
                    // to add a new weight cell,
                    // turn this cell into inputable cell, and create a new cell
                    RstWeightCell.this.realise(0, "");
                    RstWeightsFrame.this.descriptors.add(new RstWeightCell(false));
                    break;
                default:
                    RstWeightsFrame.this.main.write(RstUtils.messageOf(new IllegalAccessException("action")));
                    break;
                }
                // manipulate weight cells will not update the config dialog
                // automatically, trigger the update of GUI component
                this.redraw();
            }

            /**
             * Triggers redraw of the descriptors component of the weights
             * configuration dialog.
             */
            private void redraw() {
                RstWeightsFrame.this.descriptors.revalidate();
                RstWeightsFrame.this.descriptors.repaint();
            }
        }

        /**
         * Event handler of the weight text input of a {@code RstWeightCell}.
         */
        private class CellValidation implements DocumentListener {

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // perform realtime syntax checking for weight descriptor statement
                // syntax check on initialisation will be performed in the realise stage
                if (RstWeightCell.this.isRealised()) {
                    try {
                        // use the syntax checking function in the normalise method
                        RstUtils.normalise(new SimpleEntry<>(RstWeightCell.this.getStatement(), RstWeightCell.this.getWeight()));
                        RstWeightCell.this.statement.setBackground(Color.WHITE);
                    }
                    catch (RuntimeException ex) {
                        // syntax error visual notification
                        RstWeightCell.this.statement.setBackground(Color.PINK);
                    }
                }
            }
        }

        /**
         * Initialises a weight descriptor container unit.
         *
         * @param realise directly realises this cell when {@code true}
         */
        public RstWeightCell(boolean realise) {
            // lock realising state
            this.realised = false;
            // create the subelements
            this.action = new JButton(RstWeightsFrame.ADD);
            this.action.setFont(RstMainFrame.MONOBOLD);
            this.weight = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
            this.weight.setFont(RstMainFrame.MONO);
            this.statement = new JTextField(16);
            this.statement.setFont(RstMainFrame.MONO);

            // setup the GUI appearance
            this.setMinimumSize(RstWeightsFrame.CELL_SIZE);
            this.setPreferredSize(RstWeightsFrame.CELL_SIZE);
            this.setMaximumSize(RstWeightsFrame.CELL_SIZE);
            this.setBorder(BorderFactory.createEtchedBorder());
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.action.setMargin(new Insets(0, 0, 0, 0));
            this.action.setMinimumSize(RstWeightsFrame.ACTION_SIZE);
            this.action.setPreferredSize(RstWeightsFrame.ACTION_SIZE);
            this.action.setMaximumSize(RstWeightsFrame.ACTION_SIZE);
            ((JSpinner.NumberEditor) this.weight.getEditor()).getTextField().setColumns(4);

            // setup events
            this.action.addActionListener(this.new CellOperation());
            this.statement.getDocument().addDocumentListener(this.new CellValidation());

            this.add(this.action);
            if (realise) {
                this.realise(0, "");
            }
        }

        private final JButton action;
        private final JSpinner weight;
        private final JTextField statement;

        private boolean realised;

        /**
         * Returns {@code true} if this cell has finished realising.
         *
         * @return {@code true} if this cell has finished realising; or
         *         {@code false} otherwise
         */
        public boolean isRealised() {
            return this.realised;
        }

        /**
         * Returns the weight value of this cell.
         *
         * @return the weight value of this cell
         */
        public int getWeight() {
            return (int) this.weight.getValue();
        }

        /**
         * Returns the statement of this cell.
         *
         * @return the statement of this cell
         */
        public String getStatement() {
            return this.statement.getText();
        }

        /**
         * Realises this cell with the given values.
         *
         * @param weight the weight of the descriptor
         * @param statement the statement of the descriptor
         */
        public void realise(int weight, String statement) {
            this.action.setText(RstWeightsFrame.REMOVE);
            this.setCell(weight, statement);
            this.add(this.weight);
            this.add(this.statement);
            this.realise(new SimpleEntry<>(statement, weight));
        }

        /**
         * Returns the string representation of this cell. Will return the
         * normalised form if possible.
         *
         * @return the normalised descriptor text of this cell; or the raw
         *         descriptor text if the descriptor statement contains syntax
         *         error
         */
        @Override
        public String toString() {
            try {
                return this.weight.getValue() + ":" + RstUtils.normalise(this.statement.getText());
            }
            catch (RuntimeException e) {
                // syntax error
                return this.weight.getValue() + ":" + this.statement.getText();
            }
        }

        /**
         * Set the weight and statement of this cell
         *
         * @param weight the weight of the descriptor
         * @param statement the statement of the descriptor
         */
        private void setCell(int weight, String statement) {
            this.weight.setValue(weight);
            this.statement.setText(statement);
        }

        /**
         * Performs the cell action bound the the action button.
         */
        private void action() {
            this.action.getActionListeners()[0].actionPerformed(null);
        }

        /**
         * Realises this cell with a weight descriptor entry.
         *
         * @param entry the weight descriptor entry
         */
        private void realise(Entry<String, Integer> entry) {
            this.statement.setText(entry.getKey());
            try {
                RstUtils.normalise(entry);
                this.weight.setValue(entry.getValue());
                this.statement.setBackground(Color.WHITE);
            }
            catch (RuntimeException e) {
                // syntax error, set colour for visual notification and print on
                // main frame output
                RstWeightsFrame.this.main.write(RstUtils.messageOf(e));
                this.weight.setValue(0);
                this.statement.setBackground(Color.PINK);
            }
            // unset realising lock
            this.realised = true;
        }

    }

    /**
     * Initialises the weight descriptor configuration dialog.
     *
     * @param main the controller of the main GUI
     * @param parent the reference to the parent JFrame (RstMainFrame)
     */
    public RstWeightsFrame(RstMainFrame main, JFrame parent) {
        this.main = main;
        this.parent = parent;
        // setup the GUI appearance
        this.frame = new JDialog(this.parent, "RST2 - Config weights", true);
        this.frame.setSize(420, 540);
        this.frame.setResizable(false);
        this.frame.setLocationRelativeTo(this.parent);
        this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        // create subelements
        this.descriptors = new JPanel();
        descriptors.setLayout(new BoxLayout(this.descriptors, BoxLayout.PAGE_AXIS));
        JScrollPane scrollPane = new JScrollPane(this.descriptors,
                                                 ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                                 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setAutoscrolls(true);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);
        this.frame.add(scrollPane);
        JPanel buttons = new JPanel(new GridLayout(1, 2));
        JButton commit = new JButton("Commit");
        commit.setFont(RstMainFrame.MONOBOLD);
        JButton abort = new JButton("Discard");
        abort.setFont(RstMainFrame.MONOBOLD);
        this.frame.add(buttons, BorderLayout.SOUTH);
        buttons.add(commit);
        buttons.add(abort);

        // setup events
        commit.addActionListener(e -> this.commit());
        abort.addActionListener(e -> this.abort());
    }

    /** The size of a cell in the weight descriptor configuration dialog. */
    private static final Dimension CELL_SIZE = new Dimension(388, 24);
    /** The size of the action button in a cell. */
    private static final Dimension ACTION_SIZE = new Dimension(56, 24);
    /** String constant of the {@code Remove} action. */
    private static final String REMOVE = "Remove";
    /** String constant of the {@code Add} action. */
    private static final String ADD = "Add";

    /** Reference to the main window's controller. */
    private RstMainFrame main;
    /** Reference to the main window's frame. */
    private JFrame parent;
    /** Reference to the weight configuration dialog. */
    private JDialog frame;
    /** Panel for holding all weight configuration cells. */
    private JPanel descriptors;

    /**
     * Closes this configuration dialog and do not update the weight descriptor.
     */
    public void abort() {
        this.frame.setVisible(false);
    }

    /**
     * Closes this configuration dialog and update the weight descriptor.
     */
    public void commit() {
        String descriptor = this.toString();
        this.main.updateWeights(descriptor);
        this.main.write("updated weights descriptor");
        this.main.write(descriptor);
        this.frame.setVisible(false);
    }

    /**
     * Opens this configuration dialog and load in the settings from the main
     * GUI.
     *
     * @param weights the weights descriptor string
     */
    public void config(String weights) {
        this.load(RstUtils.dissect(weights));
        this.frame.setLocationRelativeTo(this.parent);
        this.frame.setVisible(true);
    }

    /**
     * Loads in the weight descriptor cells.
     *
     * @param list the list of descriptor entries
     */
    private void load(List<Entry<String, Integer>> list) {
        this.descriptors.removeAll();
        this.descriptors.add(new RstWeightCell(false));
        for (int i = 0; i < list.size(); i++) {
            Entry<String, Integer> entry = list.get(i);
            RstWeightCell cell = (RstWeightCell) this.descriptors.getComponent(i);
            cell.action();
            cell.realise(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Returns the weight descriptor string of the settings from this
     * configuration dialog.
     * @return the weight descriptor string of the settings from this
     *         configuration dialog
     */
    @Override
    public String toString() {
        return Arrays.stream(this.descriptors.getComponents())
                     .map(RstWeightCell.class::cast)
                     .filter(RstWeightCell::isRealised)
                     .map(RstWeightCell::toString)
                     .collect(Collectors.joining(";"));
    }

}
