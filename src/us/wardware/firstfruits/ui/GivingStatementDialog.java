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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.TextAction;

import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.GivingStatementWriter;
import us.wardware.firstfruits.fileio.XlsxFileFilter;


public class GivingStatementDialog extends JDialog
{
    private AutoComboBox nameCombo;
    private File outputFile;
    private JButton printButton;
    private JButton saveButton;
    
    public GivingStatementDialog(JFrame owner)
    {
        super(owner);
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);
        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(GivingStatementDialog.class.getResource("/icons/report16.png")).getImage());
        setIconImages(icons);
        setTitle("Giving statement");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 150));
        
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;        
        final JLabel nameLabel = new JLabel("Name: ");
        add(nameLabel, c);
        nameCombo = new AutoComboBox(new ArrayList<String>());
        nameCombo.setCaseSensitive(false);
        nameCombo.setStrict(false);
        nameCombo.setDataList(RecordManager.getInstance().getReportNameList());
        nameCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (!nameCombo.getSelectedItem().toString().trim().isEmpty()) {
                    printButton.setEnabled(true);
                    saveButton.setEnabled(true);
                } else {
                    printButton.setEnabled(false);
                    saveButton.setEnabled(false);
                }
            }
        });
        c.gridx = 1;
        c.gridwidth = 3;
        add(nameCombo, c);
        
        final JPanel buttonPanel = new JPanel();
        printButton = new JButton(new TextAction("Print"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                printReport();
                setCursor(Cursor.getDefaultCursor());
            }
        });
        printButton.setEnabled(false);
        buttonPanel.add(printButton);
        
        saveButton = new JButton(new TextAction("Save"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (chooseOutputFile()) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        runReport();
                        setCursor(Cursor.getDefaultCursor());
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(GivingStatementDialog.this, "Error occurred while running report: " + e.getMessage(), "Run report error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        saveButton.setEnabled(false);
        saveButton.setDefaultCapable(true);
        
        buttonPanel.add(saveButton);
        
        final JButton closeButton = new JButton(new TextAction("Close"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        c.gridx = 1;
        c.gridy = 2;        
        c.gridwidth = 2;
        c.fill = GridBagConstraints.EAST;
        add(buttonPanel, c);
        invalidate();
        pack();
    }
    
    private boolean chooseOutputFile()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new XlsxFileFilter());                    
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            if (FileUtils.getExtension(outputFile) == null) {
                outputFile = new File(outputFile.getAbsolutePath().concat("." + FileUtils.XLSX));
            }
            if (!nameCombo.getSelectedItem().toString().trim().isEmpty()) {
                saveButton.setEnabled(true);
            }
            return true;
        }
        outputFile = null;
        return false;
    }
    
    private void printReport()
    {
        final String selectedName = (String) nameCombo.getSelectedItem();
        if (!selectedName.isEmpty()) {
            final String lastName = selectedName.split(",")[0].trim();
            final String firstName = selectedName.split(",")[1].trim();
            GivingStatementWriter.printGivingStatement(lastName, firstName);
        }
    }
    
    private void runReport() throws IOException
    {
        final String selectedName = (String) nameCombo.getSelectedItem();
        if (!selectedName.isEmpty()) {
            final String lastName = selectedName.split(",")[0].trim();
            final String firstName = selectedName.split(",")[1].trim();
            GivingStatementWriter.writeGivingStatement(lastName, firstName, outputFile);
        }
    }
}
