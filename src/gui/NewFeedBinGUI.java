package gui;

import controller.ControllerFeedBin;
import controller.ModelFeedBin;
import supervisor.ControllerSupervisor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NewFeedBinGUI extends JFrame {

    private JPanel panelMain;
    private JButton buttonBinController;
    private JButton buttonSupervisor;
    private JButton buttonExit;
    private JPanel panelBinController;
    private JPanel panelSupervisor;
    private JComboBox<String> comboBoxBinSelection;
    private JLabel labelComboBoxBinSelection;
    private JTextField textFieldAddProduct;
    private JButton buttonAddProduct;
    private JButton buttonFlushBin;

    private ModelFeedBin[] bins;

    private ControllerFeedBin controller;
    private ControllerSupervisor supervisor;

    private ExecutorService controllerService;
    public static CountDownLatch controllerLatch;
    public static CountDownLatch exitLatch;

    public NewFeedBinGUI() {

        initNonGUIComponents();
        initGUIComponents();

        buttonExit.addActionListener(e -> {

            boolean shutdown = false;

            try {
                controllerLatch.countDown();
                shutdown = exitLatch.await(5L, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {}

            if (! shutdown) System.err.println("Error : Could not stop threads gracefully!");

            System.exit(shutdown ? 0 : -1); // Shutdown regardless of error, as JVM should be able to terminate process

        });

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                buttonExit.doClick(); // Overriding the close button to try and force graceful shutdown
            }

        });

        buttonBinController.addActionListener(e -> {

            this.panelSupervisor.setVisible(false);
            this.panelBinController.setVisible(true);

        });

        buttonSupervisor.addActionListener(e -> {

            this.panelBinController.setVisible(false);
            this.panelSupervisor.setVisible(true);

        });

        buttonAddProduct.addActionListener(e -> {

            try {

                double volume = Double.parseDouble(textFieldAddProduct.getText());
                this.controller.issueOrder(comboBoxBinSelection.getSelectedIndex(), 2, String.valueOf(volume));

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Adding product input must be a number!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);

            }

        });

        buttonFlushBin.addActionListener(e -> {

            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you would like to flush " + comboBoxBinSelection.getSelectedItem() + "?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (option == JOptionPane.YES_OPTION)
                this.controller.issueOrder(comboBoxBinSelection.getSelectedIndex(), 1, null);

        });

    }

    private void initGUIComponents() {

        this.buttonBinController.setText("Bin Controller");
        this.buttonSupervisor.setText("Supervisor");
        this.buttonExit.setText("Exit");

        this.labelComboBoxBinSelection.setText("Bin Selection");
        for (int i = 0; i < bins.length; i++) this.comboBoxBinSelection.addItem("Feed Bin #" + (i + 1));

        this.buttonAddProduct.setText("Add Product");
        this.buttonFlushBin.setText("Flush Bin");

        this.setContentPane(panelMain);
        this.setTitle("Feed Bin Demo");
        this.setPreferredSize(new Dimension(400, 200));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null); // Spawns GUI at the center of the screen
        this.panelSupervisor.setVisible(false);
        this.panelBinController.setVisible(true);
        this.pack();
        this.setVisible(true);

    }

    private void initNonGUIComponents() {

        this.bins = new ModelFeedBin[3]; // 3 bins as defined in the specification

        this.bins[0] = new ModelFeedBin(0, "Cornmeal"); // Manually declaring starting bins for demo
        this.bins[1] = new ModelFeedBin(1, "Crushed Flakes");
        this.bins[2] = new ModelFeedBin(2, "Crushed Flakes");

        this.controller = new ControllerFeedBin(bins);
        this.supervisor = new ControllerSupervisor(bins);

        this.controllerService = Executors.newFixedThreadPool(2); // 2 controllers, so 2 threads.
        controllerLatch = new CountDownLatch(1); // Used to tell threads to shut down.

        exitLatch = new CountDownLatch(2); // The two threads will trigger this when the program exits.

        this.controllerService.submit(controller);
        this.controllerService.submit(supervisor);

        this.controllerService.shutdown(); // Just tells the JVM that the service will not be accepting new submits

    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.err.println("Warning: No system look-and-feel found, using default Swing look-and-feel");
        }

        SwingUtilities.invokeLater(NewFeedBinGUI::new);

    }

}
