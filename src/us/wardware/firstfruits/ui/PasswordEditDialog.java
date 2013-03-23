package us.wardware.firstfruits.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.TextAction;

import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.GivingStatementWriter;
import us.wardware.firstfruits.fileio.XlsxFileFilter;


public class PasswordEditDialog extends JDialog
{
    private JButton okButton;
    private JButton cancelButton;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    
    public PasswordEditDialog(JFrame owner)
    {
        super(owner);
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);
        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(PasswordEditDialog.class.getResource("/icons/lock_edit.png")).getImage());
        setIconImages(icons);
        setTitle("First Fruits File Password");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setMinimumSize(new Dimension(300, 150));
        
        final JLabel currentPasswordLabel = new JLabel("Current Password: ");
        add(currentPasswordLabel, Gbc.xyi(0, 0, 2).left(10).top(10));
        currentPasswordField = new JPasswordField(15);
        add(currentPasswordField, Gbc.xyi(1, 0, 2).top(10).horizontal());
        
        final JLabel newPasswordLabel = new JLabel("New Password: ");
        add(newPasswordLabel, Gbc.xyi(0, 1, 2).left(10));
        newPasswordField = new JPasswordField(15);
        add(newPasswordField, Gbc.xyi(1, 1, 2).horizontal());
        
        final JLabel confirmPasswordLabel = new JLabel("Confirm Password: ");
        add(confirmPasswordLabel, Gbc.xyi(0, 2, 2).left(10));
        confirmPasswordField = new JPasswordField(15);
        add(confirmPasswordField, Gbc.xyi(1, 2, 2).horizontal());
        
        final JPanel buttonPanel = new JPanel();
        okButton = new JButton(new TextAction("Ok"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(!checkPasswordInput()) {
                    JOptionPane.showMessageDialog(PasswordEditDialog.this, "Passwords do not match");
                }
            }
        });
        okButton.setEnabled(true);
        okButton.setDefaultCapable(true);
        buttonPanel.add(okButton);
        
        final JButton closeButton = new JButton(new TextAction("Close"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        add(buttonPanel, Gbc.xyi(0,3,2).top(10).gridWidth(2));
        invalidate();
        pack();
    }
    
    private boolean checkPasswordInput()
    {
        char[] newPassword = newPasswordField.getPassword(); 
        char[] confirmPassword = confirmPasswordField.getPassword(); 
        boolean isCorrect = true;
        if (newPassword.length != confirmPassword.length) {
            isCorrect = false;
        } else {
            isCorrect = Arrays.equals(newPassword, confirmPassword);
        }

        Arrays.fill(newPassword,'0');
        Arrays.fill(confirmPassword,'0');

        return isCorrect;
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final PasswordEditDialog dialog = new PasswordEditDialog(null);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }
}
