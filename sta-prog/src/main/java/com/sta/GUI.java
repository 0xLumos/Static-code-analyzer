package com.sat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

class GUI implements Reportable {

    public GUI(SourceAnalyzer sourceLocation) {
        // Create the Frame
        JFrame jframe = new JFrame("STA Tool");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(1000, 1000);

        // Create the menubar with FILE and HELP
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("FILE");
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        // Add options in FILE menu
        JMenuItem fileMenu1 = new JMenuItem("New File");
        JMenuItem fileMenu2 = new JMenuItem("Save As");
        fileMenu.add(fileMenu1);
        fileMenu.add(fileMenu2);

        // Text Area at the Center
        JTextArea textArea = new JTextArea(printWelcomeMessage());                // Will contain the scan results
        textArea.setEditable(false); // Make it read-only for displaying messages

        // Add a JScrollPane to the text area
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Create the panel at bottom and add label, textArea and buttons
        JPanel panel = new JPanel(); // This panel is not visible in output
        JLabel label = new JLabel("Github URL: ");
        JTextField textField = new JTextField(15); // Accepts up to 15 characters
        JButton btn_send = new JButton("Fetch & Analyze");
        JButton btn_reset = new JButton("Reset");
        
        // Action for Send button
        btn_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = textField.getText();
                if (!url.isEmpty()) {
                        if (url.contains("https://github.com")) {
                                
                                sourceLocation.getSourceCode(url);
                                sourceLocation.analyzeSourceCode();
                                textArea.append("\n\nAnalyzing file: " + sourceLocation.getOutFile() + "\n\n");
                                try {
                                    File myObj = new File(sourceLocation.getOutFile());
                                    Scanner myReader = new Scanner(myObj);
                                    while (myReader.hasNextLine()) {
                                        String data = myReader.nextLine();
                                        textArea.append(data + "\n");
                                        
                                    }
                                    myReader.close();
                                    } catch (FileNotFoundException error) {
                                    error.printStackTrace();
                                    }
  

                            }
                        else{
                                textArea.append("Not a valid Github URL\n");
                        }
                }
                 else {
                    textArea.append("Please enter a Github URL.\n");
                }
            }
        });
        
        // Action for Reset button
        btn_reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textField.setText(""); // Clear the text field
                textArea.setText(""); // Reset the text area
            }
        });

        panel.add(label); // Components Added using Flow Layout
        panel.add(textField);
        panel.add(btn_send);
        panel.add(btn_reset);

        // Adding Components to the frame
        jframe.getContentPane().add(BorderLayout.SOUTH, panel);
        jframe.getContentPane().add(BorderLayout.NORTH, menuBar);
        jframe.getContentPane().add(BorderLayout.CENTER, scrollPane); // Add the scrollPane instead of textArea
        jframe.setVisible(true);
    
    }

    @Override
    public String generateReport() {
        return "";
    }

    private String printWelcomeMessage(){
        StringBuilder string = new StringBuilder();
        string.append("                                                                                    ");
        string.append("                                                               Static Analysis Tool\n");
        string.append("This tool analyzes code statically, identifying errors, and naming conventions, it saves ");
        string.append("the results in a file, and reads from it to the GUI screen\n\n");
        string.append("Usage: \n\n");
        string.append("Type a valid GitHub link to a repository, then click the 'Fetch & Analyze' button \n\n");
        string.append("NOTE*** All source files are stored in C://SAT-TESTS, Folders are named based on their clone date");
        string.append(" in the following format: yyyyMMddHHmmss\n\n");
        string.append("Developer: Nour Alhouseini\n\n");
        return string.toString();
    }


}
