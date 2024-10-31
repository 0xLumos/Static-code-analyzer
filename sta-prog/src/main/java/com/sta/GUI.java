package com.sat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

class GUI {
    /* This class is responsible for initalizing the GUI, it takes a parameter of type SourceAnalyzer,
    and display data such as local and remote repo paths, results file path, etc..*/

    public GUI(SourceAnalyzer sourceLocation) {
        // Create the Frame
        JFrame jframe = new JFrame("SAT Tool");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(1000, 1000);


        // Text Pane at the Center with StyledDocument for colored text
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setToolTipText("Output displays analysis results grouped by warning types.");
        StyledDocument doc = textPane.getStyledDocument();

        // Add a JScrollPane to the text pane
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textPane.setText(printWelcomeMessage());
        // Panel at bottom with label, text field, and buttons
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Github URL: ");
        JTextField textField = new JTextField(30); // Accepts up to 30 characters
        JButton btn_send = new JButton("Fetch & Analyze");
        
        JButton btn_reset = new JButton("Reset");

        btn_send.setToolTipText("Fetch and analyze source code from the provided GitHub URL.");
        btn_reset.setToolTipText("Clear the GitHub URL field and the output area.");

        // Define risk levels for warning types
        // HashMap<String, Color> riskColorMap = new HashMap<>();
        // riskColorMap.put("UseExplicitTypes", Color.RED);  // High risk
        // riskColorMap.put("ShortMethodName", Color.ORANGE); // Medium risk
        // riskColorMap.put("UselessParentheses", Color.YELLOW); // Low risk
        // Add other warning types and their colors accordingly...

        // Send button action with loading screen
        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = textField.getText().trim();
                if (validateUrl(url)) {

                    // Loading dialog
                    JDialog loadingDialog = new JDialog(jframe, "SAT Tool", true);
                    loadingDialog.setLayout(new BorderLayout());
                    loadingDialog.add(new JLabel("Analyzing, please wait...", SwingConstants.CENTER), BorderLayout.CENTER);
                    loadingDialog.setSize(200, 100);
                    loadingDialog.setLocationRelativeTo(jframe);

                    JProgressBar progressBar = new JProgressBar();
                    progressBar.setIndeterminate(true);
                    loadingDialog.add(progressBar, BorderLayout.SOUTH);

                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() {
                            sourceLocation.getSourceCode(url);
                            sourceLocation.analyzeSourceCode();
                            try (Scanner myReader = new Scanner(new File(sourceLocation.getOutFile()))) {
                                HashMap<String, ArrayList<String>> categorizedWarnings = new HashMap<>();
                                while (myReader.hasNextLine()) {
                                    String data = myReader.nextLine();
                                    Pattern pattern = Pattern.compile("(?<=\\t)[A-Za-z]+(?=:\\s)");
                                    Matcher matcher = pattern.matcher(data);
                                    if (matcher.find()) {
                                        String warningType = matcher.group();
                                        categorizedWarnings.putIfAbsent(warningType, new ArrayList<>());
                                        categorizedWarnings.get(warningType).add(data);
                                    }
                                }

                                doc.insertString(doc.getLength(), "\nAnalyzing file: " + sourceLocation.getOutFile() + "\n\n", null);

                                // Display categorized warnings with color-coding based on risk level
                                for (String warningType : categorizedWarnings.keySet()) {
                                    ArrayList<String> warnings = categorizedWarnings.get(warningType);
                                    SimpleAttributeSet set = new SimpleAttributeSet();
                                    
                                    doc.insertString(doc.getLength(), "\n" + warningType + " (" + warnings.size() + " occurrences):\n", set);
                                    for (String warning : warnings) {
                                        doc.insertString(doc.getLength(), warning + "\n", set);
                                    }
                                }

                            } catch (FileNotFoundException ex) {
                                JOptionPane.showMessageDialog(jframe, "Error: Output file not found!", "Error", JOptionPane.ERROR_MESSAGE);
                                ex.printStackTrace();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                            } catch (InterruptedException | ExecutionException ex) {
                                ex.printStackTrace();
                            }
                            loadingDialog.dispose();
                        }
                    };

                    SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
                    worker.execute();

                } else {
                    JOptionPane.showMessageDialog(jframe, "Please enter a valid GitHub URL.", "Invalid URL", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Reset button action
        btn_reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textField.setText("");
                textPane.setText(printWelcomeMessage());
            }
        });

        panel.add(label);
        panel.add(textField);
        panel.add(btn_send);
        panel.add(btn_reset);

        // Adding components to the frame
        jframe.getContentPane().add(BorderLayout.SOUTH, panel);
        jframe.getContentPane().add(BorderLayout.CENTER, scrollPane);
        jframe.setVisible(true);
    }



    private String printWelcomeMessage() {
        StringBuilder string = new StringBuilder();
        string.append("                                                                                   \n");
        string.append("                            Welcome to the Static Analysis Tool                      \n");
        string.append("===================================================================================\n");
        string.append("This application performs static code analysis to identify potential issues, naming   \n");
        string.append("convention violations, and other coding standards within your Java projects. The    \n");
        string.append("analysis results are saved to a designated file, which can be viewed in the GUI.    \n\n");
        string.append("Usage Instructions:                                                                \n");
        string.append("1. Enter a valid GitHub repository URL in the text field provided.                 \n");
        string.append("2. Click the 'Fetch & Analyze' button to initiate the analysis.                    \n\n");
        string.append("Important Note:                                                                    \n");
        string.append("All source files are stored in the 'C://SAT-TESTS' directory. Folders are named    \n");
        string.append("based on their clone date in the format: yyyy-MM-dd-HH-mm-ssSS.                          \n\n");
        string.append("Developer: Nour Alhouseini                                                          \n");
        string.append("===================================================================================\n");
        return string.toString();
    }
    
    

    private boolean validateUrl(String url) {
        return url.startsWith("https://github.com/") && url.split("/").length >= 5;
    }
}
