package us.wardware.firstfruits.ui;

import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class FilePasswordPanel extends JPanel
{
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private boolean changingPassword;
    private char[] currentPassword;
    private String error;
    private String newPassword;
    
    public FilePasswordPanel(char[] password)
    {
        this.changingPassword = password.length > 0;
        this.currentPassword = password;
        newPassword = "";
        initComponents();
    }
    
    public boolean isChangingPassword()
    {
        return changingPassword;
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        
        int gridy = 0;
        if (changingPassword) {
            final JLabel currentPasswordLabel = new JLabel("Current Password: ");
            add(currentPasswordLabel, Gbc.xyi(0, gridy, 2).west());
            currentPasswordField = new JPasswordField(15);
            add(currentPasswordField, Gbc.xyi(1, gridy, 2).horizontal());
            ++gridy;
        }
        
        final JLabel newPasswordLabel = new JLabel("New Password: ");
        add(newPasswordLabel, Gbc.xyi(0, gridy, 2).west());
        newPasswordField = new JPasswordField(15);
        add(newPasswordField, Gbc.xyi(1, gridy, 2).horizontal());
        
        ++gridy;
        final JLabel confirmPasswordLabel = new JLabel("Confirm Password: ");
        add(confirmPasswordLabel, Gbc.xyi(0, gridy, 2).west());
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField, Gbc.xyi(1, gridy, 2).horizontal());
        ++gridy;
        
        final StringBuilder sb = new StringBuilder();
        sb.append("<HTML>");
        if (changingPassword) {
            sb.append("To remove current password, leave the New and Confirm fields empty.");
        }
        sb.append("<BR>It is recommended you use a password that is at least 8 characters.");
        sb.append("<BR><B>NOTE:</B> All passwords are case sensative.");
        sb.append("</HTML>");
        add(new JLabel(sb.toString()), Gbc.xyi(0, gridy, 2).top(5).left(10).gridWidth(2).gridHeight(3));
        
        updateUI();
        revalidate();
        repaint();
    }
    
    public boolean checkPasswordInput()
    {
        final char[] newPasswordEntry = newPasswordField.getPassword(); 
        final char[] confirmPasswordEntry = confirmPasswordField.getPassword(); 
        boolean isCorrect = true;
        if (newPasswordEntry.length != confirmPasswordEntry.length) {
            isCorrect = false;
            error = "New password and Confirm password entries do not match.";
        } else if (newPasswordEntry.length == 0 && confirmPasswordEntry.length == 0) {
            if (changingPassword) {
                final char[] currentPasswordEntry = currentPasswordField.getPassword(); 
                isCorrect = Arrays.equals(currentPassword, currentPasswordEntry);
                if (!isCorrect) {
                    error = "The current password provided is not correct.";
                }
            } else {
                isCorrect = false;
                error = "Password fields are empty.";
            }
        } else if (changingPassword) {
            final char[] currentPasswordEntry = currentPasswordField.getPassword(); 
            isCorrect = Arrays.equals(currentPassword, currentPasswordEntry);
            if (isCorrect) {
                isCorrect = Arrays.equals(newPasswordEntry, confirmPasswordEntry);
                if (!isCorrect) {
                    error = "New password and Confirm password entries do not match.";
                }
            } else {
                error = "Current password is not correct.";
            }
        } else {
           isCorrect = Arrays.equals(newPasswordEntry, confirmPasswordEntry);
           if (!isCorrect) {
               error = "New password and Confirm password entries do not match.";
           }
        }

        if (isCorrect) {
            this.newPassword = new String(newPasswordEntry);
        }
        
        Arrays.fill(newPasswordEntry,'0');
        Arrays.fill(confirmPasswordEntry,'0');

        return isCorrect;
    }
    
    public String getError()
    {
        return error;
    }
    
    public String getNewPassword()
    {
        return newPassword;
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
                
                testPasswordPanel("");
                testPasswordPanel("abc");
            }

            private void testPasswordPanel(String password)
            {
                boolean passwordChangeComplete = false;
                final FilePasswordPanel fpp = new FilePasswordPanel(password.toCharArray()); 
                while (!passwordChangeComplete) {
                    int answer = JOptionPane.showConfirmDialog(  
                                    null, fpp, "File Password", JOptionPane.OK_CANCEL_OPTION,  
                                    JOptionPane.PLAIN_MESSAGE  
                                    );  
                    if (answer == JOptionPane.YES_OPTION)  
                    {  
                        if (fpp.checkPasswordInput()) {
                            if (!fpp.getNewPassword().isEmpty() && fpp.isChangingPassword()) {
                                JOptionPane.showMessageDialog(null, "Password successfully changed");
                            } else if (!fpp.getNewPassword().isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Password successfully added");
                            } else {
                                JOptionPane.showMessageDialog(null, "Password successfully removed");
                            }
                            passwordChangeComplete = true;
                        } else {
                            JOptionPane.showMessageDialog(null, fpp.getError(), "Password Failure", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        passwordChangeComplete = true;
                    }
                }
            }
        });
    }
}
