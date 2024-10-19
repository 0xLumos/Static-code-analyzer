import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

 
class GUI {

 
    public GUI(){
                //Create the Frame
                JFrame jframe = new JFrame("Chat Screen");
                jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                jframe.setSize(400, 400);
        
        
        
         
        //     create two menubar button FILE and HELP
                JMenuBar menuBar = new JMenuBar();
                JMenu fileMenu = new JMenu("FILE");
                JMenu helpMenu = new JMenu("Help");
                menuBar.add(fileMenu);
                menuBar.add(helpMenu);
        
         
        //      create two more option in FILE button
                JMenuItem fileMenu1 = new JMenuItem("new file");
                JMenuItem fileMenu2 = new JMenuItem("Save as");
                fileMenu.add(fileMenu1);
               fileMenu.add(fileMenu2);
        
         
                // Text Area at the Center
                JTextArea textArea = new JTextArea("STA");
        
                //Create the panel at bottom and add label, textArea and buttons
                JPanel panel = new JPanel(); // this panel is not visible in output
                JLabel label = new JLabel("Github URL: ");
                JTextField textField = new JTextField(15); // accepts upto 15 characters
                JButton btn_send = new JButton("Send");
                JButton btn_reset = new JButton("Reset");
                panel.add(label); // Components Added using Flow Layout
                panel.add(textField);
                panel.add(btn_send);
                panel.add(btn_reset);
        
         
                //Adding Components to the frame.
                jframe.getContentPane().add(BorderLayout.SOUTH, panel);
                jframe.getContentPane().add(BorderLayout.NORTH, menuBar);
                jframe.getContentPane().add(BorderLayout.CENTER, textArea);
                jframe.setVisible(true);
        
         
        
    }

            public static void main(String[] args) {
                GUI gui = new GUI();
                
            }
 
}
