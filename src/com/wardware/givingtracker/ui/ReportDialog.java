package com.wardware.givingtracker.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.text.TextAction;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.fileio.CsvFileFilter;
import com.wardware.givingtracker.fileio.FileUtils;
import com.wardware.givingtracker.fileio.ReportWriter;

public class ReportDialog extends JDialog
{
    private AutoComboBox nameCombo;
    private File outputFile;
    private JLabel outputFileLabel;
    private JButton runReportButton;
    
    public ReportDialog()
    {
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);

        setTitle("Run Report");
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
        nameCombo.setDataList(RecordManager.getInstance().getUniqueNames());
        nameCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                if (!nameCombo.getSelectedItem().toString().trim().isEmpty() &&
                    outputFile != null) {
                    runReportButton.setEnabled(true);
                } else {
                    runReportButton.setEnabled(false);
                }
            }
        });
        c.gridx = 1;
        c.gridwidth = 3;
        add(nameCombo, c);
        
        final JButton outputFileButton = new JButton(new TextAction("Output File: "){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chooseOutputFile();
            }
        });
        c.gridx = 0;
        c.gridy = 1;        
        c.gridwidth = 1;
        add(outputFileButton, c);
        
        outputFileLabel = new JLabel();
        c.gridx = 1;
        c.gridwidth = 3;
        add(outputFileLabel, c);
        
        final JPanel buttonPanel = new JPanel();
        runReportButton = new JButton(new TextAction("Run"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReport();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ReportDialog.this, "Error occurred while running report: " + e.getMessage(), "Run report error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        runReportButton.setEnabled(false);
        
        buttonPanel.add(runReportButton);
        
        final JButton cancelButton = new JButton(new TextAction("Cancel"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        c.gridx = 2;
        c.gridy = 2;        
        c.gridwidth = 2;
        c.fill = GridBagConstraints.EAST;
        add(buttonPanel, c);
        invalidate();
        pack();
    }
    
    private void chooseOutputFile()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CsvFileFilter());                    
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = fileChooser.getSelectedFile();
            if (FileUtils.getExtension(outputFile) == null) {
                outputFile = new File(outputFile.getAbsolutePath().concat("." + FileUtils.CSV));
            }
            outputFileLabel.setText(outputFile.getAbsolutePath());
            if (!nameCombo.getSelectedItem().toString().trim().isEmpty()) {
                runReportButton.setEnabled(true);
            }
            invalidate();
            pack();
        }
    }
    
    private void runReport() throws IOException
    {
        final String selectedName = (String) nameCombo.getSelectedItem();
        final List<GivingRecord> records = RecordManager.getInstance().getRecordsForName(selectedName);
        ReportWriter.writeReport(selectedName, outputFile, records);
        setVisible(false);
        dispose();
    }
}
