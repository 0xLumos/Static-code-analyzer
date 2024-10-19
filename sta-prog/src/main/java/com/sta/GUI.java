package com.sta;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;

class GUI implements Reportable {

    public GUI(SourceAnalyzer sourceLocation) {
        // Create the Frame
        JFrame jframe = new JFrame("Chat Screen");
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setSize(400, 400);

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
        JTextArea textArea = new JTextArea("");                // Will contain the scan results
        textArea.setEditable(false); // Make it read-only for displaying messages

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
                                textArea.append("Fetching... \n");
                                sourceLocation.getSourceCode(url);
                                textArea.append("Fetching Complete  \n");
                                textArea.append("Analyzing...  \n");
                                sourceLocation.analyzeSourceCode();
                                try {
                                    File myObj = new File(sourceLocation.getOutFile());
                                    Scanner myReader = new Scanner(myObj);
                                    while (myReader.hasNextLine()) {
                                        String data = myReader.nextLine();
                                        textArea.append(data + "\n");
                                    }
                                    myReader.close();
                                    } catch (FileNotFoundException error) {
                                    System.out.println("An error occurred.");
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
                textArea.setText("STA"); // Reset the text area
            }
        });

        panel.add(label); // Components Added using Flow Layout
        panel.add(textField);
        panel.add(btn_send);
        panel.add(btn_reset);

        // Adding Components to the frame
        jframe.getContentPane().add(BorderLayout.SOUTH, panel);
        jframe.getContentPane().add(BorderLayout.NORTH, menuBar);
        jframe.getContentPane().add(BorderLayout.CENTER, textArea);
        jframe.setVisible(true);
    }

    @Override
    public String generateReport() {
        return "";
    }

    // Uncomment to run the GUI directly
    // public static void main(String[] args) {
    //     SourceAnalyzer sourceAnalyzer = new SourceAnalyzer(); // Example usage
    //     GUI gui = new GUI(sourceAnalyzer);
    // }
}
