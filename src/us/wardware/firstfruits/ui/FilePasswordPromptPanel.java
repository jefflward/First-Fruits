package us.wardware.firstfruits.ui;

import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FilePasswordPromptPanel extends JPanel
{
    private JPasswordField passwordField;
    private char[] currentPassword;
    private String error;
    
    public FilePasswordPromptPanel(char[] password)
    {
        this.currentPassword = password;
        initComponents();
    }
    
    private void initComponents()
    {
        setLayout(new GridBagLayout());
        
        int gridy = 0;
        final StringBuilder sb = new StringBuilder();
        sb.append("<HTML>");
        sb.append("This file is password protected. To open you must provide the password.");
        sb.append("<BR><B>NOTE:</B> Password is case sensative.");
        sb.append("</HTML>");
        add(new JLabel(sb.toString()), Gbc.xyi(0, gridy, 2).top(5).gridWidth(2));
        gridy++;
        
        final JLabel currentPasswordLabel = new JLabel("Password: ");
        add(currentPasswordLabel, Gbc.xyi(0, gridy, 2).west());
        passwordField = new JPasswordField(15);
        add(passwordField, Gbc.xyi(1, gridy, 2).horizontal());
    }
    
    public boolean checkPasswordInput()
    {
        final char[] passwordEntry = passwordField.getPassword(); 
        boolean isCorrect = Arrays.equals(currentPassword, passwordEntry);
        if (!isCorrect) {
            error = "The password provided is not correct.";
        }

        return isCorrect;
    }
    
    public String getError()
    {
        return error;
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                testPasswordPrompt("abc");
            }

            private void testPasswordPrompt(String password)
            {
                boolean finished = false;
                final FilePasswordPromptPanel fpp = new FilePasswordPromptPanel(password.toCharArray()); 
                while (!finished) {
                    int answer = JOptionPane.showConfirmDialog(  
                                    null, fpp, "File Password", JOptionPane.OK_CANCEL_OPTION,  
                                    JOptionPane.PLAIN_MESSAGE  
                                    );  
                    if (answer == JOptionPane.OK_OPTION)  
                    {  
                        if (fpp.checkPasswordInput()) {
                            finished = true;
                        } else {
                            JOptionPane.showMessageDialog(null, fpp.getError(), "Password Failure", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        finished = true;
                    }
                }
            }
        });
    }
}
