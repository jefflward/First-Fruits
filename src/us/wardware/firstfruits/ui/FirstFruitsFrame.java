package us.wardware.firstfruits.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.TextAction;

import org.apache.commons.lang.StringUtils;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;
import us.wardware.firstfruits.fileio.CsvFileFilter;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.GivingRecordsReader;
import us.wardware.firstfruits.fileio.GivingRecordsWriter;
import us.wardware.firstfruits.fileio.GivingRecordsReader.CategoryComparison;


public class FirstFruitsFrame extends JFrame implements Observer
{
    private InputPanel inputPanel;
    private RecordsTable recordsTable;
    private File currentFile;
    private AbstractButton deleteButton;
    private JButton saveButton;
    private JMenu reportsMenu;
    private JButton reportButton;
    private JButton reportAllButton;
    private JMenuItem saveItem;
    private JMenuItem saveAsItem;
    private JLabel lastEntryLabel;
    private JLabel recordCountLabel;
    private AbstractButton offeringReportButton;
    private TallyDialog tallyDialog;
    private SettingsDialog settingsDialog;
    
    public FirstFruitsFrame()
    {
        initComponents();
        tallyDialog = new TallyDialog(this);
        tallyDialog.setLocationRelativeTo(FirstFruitsFrame.this);
        settingsDialog = new SettingsDialog(this, false);
        settingsDialog.setLocationRelativeTo(FirstFruitsFrame.this);
        RecordManager.getInstance().addObserver(this);
    }

    private void initComponents()
    {
        setTitle("First Fruits");
        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo256.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo128.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo48.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo32.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo24.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo20.png")).getImage());
        icons.add(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo16.png")).getImage());
        setIconImages(icons);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(500, 350));
        setPreferredSize(new Dimension(1000, 600));
        setLocationByPlatform(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                handleExitConfirmation();
            }
        });
        
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        final JMenuItem newItem = new JMenuItem(new TextAction("New") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                createNew();
            }
        });
        newItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/new.png")));
        newItem.setMnemonic('N');
        fileMenu.add(newItem);
        
        final JMenuItem open = new JMenuItem(new TextAction("Open") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    openFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        open.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/open.png")));
        open.setMnemonic('O');
        fileMenu.add(open);
        
        saveItem = new JMenuItem(new TextAction("Save") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/save.png")));
        saveItem.setMnemonic('S');
        saveItem.setEnabled(false);
        fileMenu.add(saveItem);
        
        saveAsItem = new JMenuItem(new TextAction("Save As..") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveAs();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveAsItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/save_as.png")));
        saveAsItem.setMnemonic('A');
        saveAsItem.setEnabled(false);
        fileMenu.add(saveAsItem);
        
        fileMenu.add(new JSeparator());
        
        final JMenuItem settings = new JMenuItem(new TextAction("Settings") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                configureSettings();
            }
        });
        settings.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/settings.png")));
        settings.setMnemonic('E');
        fileMenu.add(settings);
        
        fileMenu.add(new JSeparator());
        
        final JMenuItem exit = new JMenuItem(new TextAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                handleExitConfirmation();
            }
        });
        exit.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/exit.png")));
        exit.setMnemonic('X');
        fileMenu.add(exit);
        
        menuBar.add(fileMenu);
        
        reportsMenu = new JMenu("Reports");
        reportsMenu.setMnemonic('R');
        reportsMenu.setEnabled(false);
        final JMenuItem reportItem = new JMenuItem(new TextAction("Giving statement") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report.png")));
        reportItem.setMnemonic('S');
        reportsMenu.add(reportItem);
        
        final JMenuItem reportAll = new JMenuItem(new TextAction("Generate All Giving Statements") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReportAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportAll.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report_all.png")));
        reportAll.setMnemonic('A');
        reportsMenu.add(reportAll);
        
        final JMenuItem offeringReport = new JMenuItem(new TextAction("Offering Report") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runOfferingReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        offeringReport.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/offering.png")));
        offeringReport.setMnemonic('O');
        reportsMenu.add(offeringReport);
        menuBar.add(reportsMenu);
        
        final JMenu toolsMenu = new JMenu("Tools");
        final JMenuItem tally = new JMenuItem(new TextAction("Quick Tally") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!tallyDialog.isVisible()) {
                            tallyDialog.setVisible(true);
                            tallyDialog.toFront();
                            tallyDialog.repaint();
                        }
                    }
                });
            }
        });
        tally.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/tally_small.png")));
        toolsMenu.add(tally);
        menuBar.add(toolsMenu); 
        
        final JMenu helpMenu = new JMenu("Help");
        final JMenuItem about = new JMenuItem(new TextAction("About") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                about();
            }
        });
        about.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo16.png")));
        helpMenu.add(about);
        menuBar.add(helpMenu); 
        setJMenuBar(menuBar);
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        inputPanel = new InputPanel();
        splitPane.setLeftComponent(inputPanel); 
        
        recordsTable = new RecordsTable();
        final JPanel right = new JPanel(new BorderLayout());
        final JPanel top = new JPanel(new BorderLayout());
        final RecordFilterSettingsPanel recordFilterSettings = new RecordFilterSettingsPanel();
        top.add(recordFilterSettings, BorderLayout.EAST);
        right.add(top, BorderLayout.NORTH);
        final JScrollPane scrollPane = new JScrollPane(recordsTable);
        right.add(scrollPane, BorderLayout.CENTER);
        splitPane.setRightComponent(right);
        
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        final JButton newButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/new.png")));
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                createNew();
            }
        });
        newButton.setToolTipText("New");
        buttonPanel.add(newButton);
        
        final JButton openButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/open.png")));
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    openFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        openButton.setToolTipText("Open");
        buttonPanel.add(openButton);
        
        saveButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/save.png")));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveButton.setToolTipText("Save");
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);
        
        final JButton settingsButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/settings.png")));
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                configureSettings();
            }
        });
        settingsButton.setToolTipText("Settings");
        buttonPanel.add(settingsButton);
        
        final JButton tallyButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/tally_small.png")));
        tallyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (!tallyDialog.isVisible()) {
                            tallyDialog.setVisible(true);
                            tallyDialog.toFront();
                            tallyDialog.repaint();
                        }
                    }
                });
            }
        });
        tallyButton.setToolTipText("Quick Tally");
        buttonPanel.add(tallyButton);
        
        reportButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report.png")));
        reportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportButton.setToolTipText("Giving statement");
        reportButton.setEnabled(false);
        buttonPanel.add(reportButton);
        
        reportAllButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report_all.png")));
        reportAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReportAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportAllButton.setToolTipText("Generate All Giving Statements");
        reportAllButton.setEnabled(false);
        buttonPanel.add(reportAllButton);
        
        offeringReportButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/offering.png")));
        offeringReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runOfferingReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        offeringReportButton.setToolTipText("Offering Report");
        offeringReportButton.setEnabled(false);
        buttonPanel.add(offeringReportButton);
        
        deleteButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/delete.png")));
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                deleteSelectedRecords();
            }
        });
        deleteButton.setToolTipText("Delete Selected Records");
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);
        
        final JPanel statusPanel = new JPanel(new BorderLayout());
        lastEntryLabel = new JLabel(" ");
        statusPanel.add(lastEntryLabel, BorderLayout.WEST);
        recordCountLabel = new JLabel(" ");
        statusPanel.add(recordCountLabel, BorderLayout.EAST);
        
        getContentPane().add(buttonPanel, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        pack();
    }
    
    protected void about()
    {
        final Properties p = new Properties();
        try {
            p.load(FirstFruitsFrame.class.getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, 
                        "<HTML><B>First Fruits</B>" +
                        "<BR>Version: " + p.getProperty("version") + 
                        "<BR>(c) Copyright WardWare 2012, 2013.  All rights reserved.<HTML>", "About First Fruits", 
                        JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo48.png")));
    }

    protected void createNew()
    {
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                            "Unsaved changes exist. Do you want to save?", "Save Changes", 
                            JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    saveFile();
                    RecordManager.getInstance().createNew();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                RecordManager.getInstance().createNew();
            }
        } else {
            RecordManager.getInstance().createNew();
        }
    }

    private void deleteSelectedRecords()
    {
        if (recordsTable.deleteSelectedRecords()) {
            if (currentFile == null && RecordManager.getInstance().getAllRecords().size() == 0) {
                RecordManager.getInstance().setUnsavedChanges(false);
            }
        }
    }
    
    private void configureSettings()
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                if (!settingsDialog.isVisible()) {
                    settingsDialog.setVisible(true);
                    settingsDialog.toFront();
                    settingsDialog.repaint();
                }
            }
        });
    }
    
    private void runReportAll() throws IOException
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final ReportAllDialog dialog = new ReportAllDialog(FirstFruitsFrame.this);
                dialog.setLocationRelativeTo(FirstFruitsFrame.this);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }
    
    private void runOfferingReport() throws IOException
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final OfferingReportDialog dialog = new OfferingReportDialog(FirstFruitsFrame.this);
                dialog.setLocationRelativeTo(FirstFruitsFrame.this);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }
    
    private void runReport() throws IOException
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final GivingStatementDialog reportDialog = new GivingStatementDialog(FirstFruitsFrame.this);
                reportDialog.setLocationRelativeTo(FirstFruitsFrame.this);
                reportDialog.setVisible(true);
            }
        });
    }
    
    private void handleExitConfirmation()
    {
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                            "Unsaved changes exist. Do you want to save?", "Save Changes", 
                            JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    saveFile();
                } catch (IOException e) {
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                setVisible(false);
                dispose();
            }
        } else {
            setVisible(false);
            dispose();
        }
    }
    
    private void openFile() throws IOException
    {
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                            "Unsaved changes exist. Do you want to save?", "Save Changes", 
                            JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    saveFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (choice == JOptionPane.CANCEL_OPTION ||
                       choice == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CsvFileFilter());
        fileChooser.setMultiSelectionEnabled(true);
        final List<GivingRecord> records = new ArrayList<GivingRecord>();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File[] selectedFiles = fileChooser.getSelectedFiles();
            if (selectedFiles.length == 1) {
                currentFile = selectedFiles[0];
            } else {
                currentFile = null;
            }
            
            for (File f : fileChooser.getSelectedFiles()) {
                try {
                    final CategoryComparison categoryComparison = GivingRecordsReader.getCategoryComparison(f);
                    if (categoryComparison.hasExtraCategories()) {
                        final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                                        "<HTML>The records being imported do not match the categories defined in the program settings.<BR>" +
                                        "The records contain the following categories:<BR>" + 
                                        "<B>[" + StringUtils.join(categoryComparison.allCategories, ", ") + "]</B><BR>" +
                                        "<BR>The records do not contain the following categories " + 
                                        "(<I>values will be zero</I>):<BR>" + 
                                        "<B>[" + StringUtils.join(categoryComparison.missingCategories, ", ") + "]</B><BR>" +
                                        "<BR>Do you want to add these categories to the settings?  If you choose 'No', data for these<BR>" +
                                        "categories will be ignored:<BR>" +
                                        "<B>[" + StringUtils.join(categoryComparison.extraCategories, ", ") + "]</B><BR>",
                                        "Category Conflicts Detected", 
                                        JOptionPane.YES_NO_CANCEL_OPTION);
                        if (choice == JOptionPane.YES_OPTION) {
                            for (String category : categoryComparison.extraCategories) {
                                Settings.getInstance().addCategory(category);
                            }
                            records.addAll(GivingRecordsReader.readRecordsFromFile(f));
                        } else if (choice == JOptionPane.NO_OPTION) {
                            records.addAll(GivingRecordsReader.readRecordsFromFile(f));
                        }
                    } else {
                        records.addAll(GivingRecordsReader.readRecordsFromFile(f));
                    }
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(FirstFruitsFrame.this, 
                                    "Error occurred while loading invalid record file.\n" + f.getAbsolutePath() +
                                    "\nline: " + e.getMessage(), 
                                    "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            RecordManager.getInstance().setRecords(records);
        }
    }
    
    private void saveAs() throws IOException
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CsvFileFilter()); 
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            if (FileUtils.getExtension(currentFile) == null) {
                currentFile = new File(currentFile.getAbsolutePath().concat("." + FileUtils.CSV));
            }
            GivingRecordsWriter.writeRecordsToFile(currentFile);
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }
    
    private void saveFile() throws IOException 
    {
        if (currentFile != null) {
            GivingRecordsWriter.writeRecordsToFile(currentFile);
        } else {
            saveAs();
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }

    @Override
    public void update(Observable arg0, Object value)
    {
        final GivingRecord lastUpdated = RecordManager.getInstance().getLastUpdatedRecord();
        if (lastUpdated != null) {
            lastEntryLabel.setText("  Last Entry: " + lastUpdated.toBasicString());
        }
        recordCountLabel.setText("Record Count: " + RecordManager.getInstance().getRecords().size() + "  ");
        deleteButton.setEnabled(RecordManager.getInstance().getSelectionCount() > 0);
        
        boolean hasRecords = RecordManager.getInstance().getAllRecords().size() > 0;
        boolean hasUnsavedChanges = RecordManager.getInstance().hasUnsavedChanges();
        saveButton.setEnabled(hasUnsavedChanges);
        saveItem.setEnabled(hasUnsavedChanges);
        saveAsItem.setEnabled(hasRecords);
        reportsMenu.setEnabled(hasRecords);
        reportButton.setEnabled(hasRecords);
        reportAllButton.setEnabled(hasRecords);
        offeringReportButton.setEnabled(hasRecords);
    }
}
