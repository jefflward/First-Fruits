package com.wardware.givingtracker.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.TextAction;

import com.wardware.givingtracker.Settings;

public class SettingsDialog extends JDialog
{
    private JTextField categoryField;
    private JTextArea addressTextArea;
    private JTextArea categoriesTextArea;

    public SettingsDialog()
    {
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);

        setTitle("Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;        
        final JLabel nameLabel = new JLabel("Organization Name");
        nameLabel.setHorizontalAlignment(JLabel.RIGHT);
        nameLabel.setVerticalAlignment(JLabel.TOP);
        add(nameLabel, c);
       
        categoryField = new JTextField();
        c.gridx = 1;
        add(categoryField, c);
        
        final JLabel addressLabel = new JLabel("Organization Address");
        addressLabel.setHorizontalAlignment(JLabel.RIGHT);
        addressLabel.setVerticalAlignment(JLabel.TOP);
        c.gridx = 0;
        c.gridy = 1;        
        add(addressLabel, c);
        
        addressTextArea = new JTextArea();
        c.gridx = 1;
        final JScrollPane addressPane = new JScrollPane(addressTextArea);
        addressPane.setPreferredSize(new Dimension(190, 80));
        add(addressPane, c);
        
        c.gridx = 0;
        c.gridy = 2;        
        c.anchor = GridBagConstraints.PAGE_START;
        final JLabel namesLabel = new JLabel("Categories");
        namesLabel.setHorizontalAlignment(JLabel.RIGHT);
        namesLabel.setVerticalAlignment(JLabel.TOP);
        add(namesLabel, c);
        
        categoriesTextArea = new JTextArea();
        c.gridx = 1;
        final JScrollPane namesPane = new JScrollPane(categoriesTextArea);
        namesPane.setPreferredSize(new Dimension(190, 200));
        add(namesPane, c);
        
        final JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton(new TextAction("Ok"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveSettings();
                setVisible(false);
                dispose();
            }
        });
        okButton.setDefaultCapable(true);
        
        buttonPanel.add(okButton);
        
        final JButton cancelButton = new JButton(new TextAction("Cancel"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        c.gridx = 0;
        c.gridy = 3;        
        c.gridwidth = 2;
        c.fill = GridBagConstraints.EAST;
        add(buttonPanel, c);
        loadSettings();
        invalidate();
        pack();
    }
    
    private void loadSettings()
    {
        final Properties props = Settings.getInstance().getProperties();
        categoryField.setText(props.getProperty(Settings.ORGANIZATION_NAME_KEY, ""));
        addressTextArea.setText(props.getProperty(Settings.ORGANIZATION_ADDRESS_KEY, ""));
        categoriesTextArea.setText(props.getProperty(Settings.CATEGORIES_KEY, "").replaceAll(";", "\n"));
    }
    
    private void saveSettings()
    {
        final Properties props = Settings.getInstance().getProperties();
        props.put(Settings.ORGANIZATION_NAME_KEY, categoryField.getText());
        props.put(Settings.ORGANIZATION_ADDRESS_KEY, addressTextArea.getText());
        props.put(Settings.CATEGORIES_KEY, categoriesTextArea.getText().replaceAll("\n", ";"));
        Settings.getInstance().setProperties(props);
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final SettingsDialog dialog = new SettingsDialog();
                dialog.setVisible(true);
            }
        });
    }
}
