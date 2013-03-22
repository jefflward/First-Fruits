package us.wardware.firstfruits.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
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

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.GivingStatementWriter;


public class ReportAllDialog extends JDialog
{
    private File outputDirectory;
    private JLabel outputFileLabel;
    private JButton runReportButton;
    
    public ReportAllDialog(JFrame owner)
    {
        super(owner);
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);
        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(GivingStatementDialog.class.getResource("/icons/report_all16.png")).getImage());
        setIconImages(icons);
        setTitle("Generate All Giving Statements");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(300, 100));
        
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        
        final JButton outputDirectory = new JButton(new TextAction("Output Directory: "){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                chooseOutputDirectory();
            }
        });
        c.gridx = 0;
        c.gridy = 0;        
        c.gridwidth = 1;
        add(outputDirectory, c);
        
        outputFileLabel = new JLabel();
        c.gridx = 1;
        c.gridwidth = 2;
        add(outputFileLabel, c);
        
        final JPanel buttonPanel = new JPanel();
        runReportButton = new JButton(new TextAction("Run"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    runReport();
                    setCursor(Cursor.getDefaultCursor());
                    setVisible(false);
                    dispose();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ReportAllDialog.this, "Error occurred while running report: " + e.getMessage(), "Run Report Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        runReportButton.setEnabled(false);
        runReportButton.setDefaultCapable(true);
        
        buttonPanel.add(runReportButton);
        
        final JButton cancelButton = new JButton(new TextAction("Close"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(cancelButton);
        c.gridx = 2;
        c.gridy = 1;        
        c.gridwidth = 2;
        c.fill = GridBagConstraints.EAST;
        add(buttonPanel, c);
        invalidate();
        pack();
    }
    
    private void chooseOutputDirectory()
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputDirectory = fileChooser.getSelectedFile();
            outputFileLabel.setText(outputDirectory.getAbsolutePath());
            runReportButton.setEnabled(true);
            invalidate();
            pack();
        }
    }
    
    private void runReport() throws IOException
    {
        final List<String> names = RecordManager.getInstance().getReportNameList();
        
        for (String name : names) {
            if (!name.isEmpty()) {
                final String lastName = name.split(",")[0].trim();
                final String firstName = name.split(",")[1].trim();
                final List<GivingRecord> records = RecordManager.getInstance().getRecordsForName(lastName, firstName);
                if (!name.trim().isEmpty() && records.size() > 0) {
                    final File outputFile = new File(outputDirectory.getAbsoluteFile() + File.separator + lastName + firstName + "." + FileUtils.XLSX);
                    GivingStatementWriter.writeGivingStatement(lastName, firstName, outputFile);
                } 
            }
        }
    }
}
