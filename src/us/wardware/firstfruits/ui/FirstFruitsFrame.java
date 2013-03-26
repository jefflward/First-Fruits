package us.wardware.firstfruits.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
import us.wardware.firstfruits.fileio.FileException;
import us.wardware.firstfruits.fileio.FileUtils;
import us.wardware.firstfruits.fileio.GivingRecordsReader;
import us.wardware.firstfruits.fileio.GivingRecordsReader.CategoryComparison;
import us.wardware.firstfruits.fileio.GivingRecordsWriter;


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
    private JMenu recentFilesMenu;
    private JMenuItem passwordProtectItem;
    private String currentPassword;
    
    public FirstFruitsFrame()
    {
        initComponents();
        currentPassword = "";
        tallyDialog = new TallyDialog(this);
        tallyDialog.setLocationRelativeTo(FirstFruitsFrame.this);
        settingsDialog = new SettingsDialog(this, false);
        settingsDialog.setLocationRelativeTo(FirstFruitsFrame.this);
        RecordManager.getInstance().addObserver(this);
        Settings.getInstance().addObserver(this);
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
        newItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/new16.png")));
        newItem.setMnemonic('N');
        fileMenu.add(newItem);
        
        final JMenuItem open = new JMenuItem(new TextAction("Open") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    openFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FileException e) {
                    JOptionPane.showMessageDialog(FirstFruitsFrame.this, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        open.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/open16.png")));
        open.setMnemonic('O');
        fileMenu.add(open);
        
        saveItem = new JMenuItem(new TextAction("Save") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveFile();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        saveItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/save16.png")));
        saveItem.setMnemonic('S');
        saveItem.setEnabled(false);
        fileMenu.add(saveItem);
        
        saveAsItem = new JMenuItem(new TextAction("Save As..") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveAs();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        saveAsItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/saveas16.png")));
        saveAsItem.setMnemonic('A');
        saveAsItem.setEnabled(false);
        fileMenu.add(saveAsItem);
        
        fileMenu.add(new JSeparator());
        
        recentFilesMenu = new JMenu("Recent Files");
        updateRecentFileList();
        fileMenu.add(recentFilesMenu);
        
        fileMenu.add(new JSeparator());
        
        passwordProtectItem = new JMenuItem(new TextAction("File password protection") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    if (setPasswordProtection()) {
                        saveFile();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        passwordProtectItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock_edit16.png")));
        passwordProtectItem.setMnemonic('P');
        passwordProtectItem.setEnabled(false);
        fileMenu.add(passwordProtectItem);
        
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
        reportItem.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report16.png")));
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
        reportAll.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report_all16.png")));
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
        offeringReport.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/offering16.png")));
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
        tally.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/calc16.png")));
        toolsMenu.add(tally);        
        
        final JMenuItem settings = new JMenuItem(new TextAction("Settings") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                configureSettings();
            }
        });
        settings.setIcon(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/settings16.png")));
        settings.setMnemonic('E');
        toolsMenu.add(new JSeparator());
        toolsMenu.add(settings);
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
        
        final JButton newButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/new24.png")));
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                createNew();
            }
        });
        newButton.setToolTipText("New");
        buttonPanel.add(newButton);
        
        final JButton openButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/open24.png")));
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    openFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (FileException e) {
                    JOptionPane.showMessageDialog(FirstFruitsFrame.this, e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        openButton.setToolTipText("Open");
        buttonPanel.add(openButton);
        
        saveButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/save24.png")));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    saveFile();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        saveButton.setToolTipText("Save");
        saveButton.setEnabled(false);
        buttonPanel.add(saveButton);
        
        final JButton settingsButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/settings24.png")));
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                configureSettings();
            }
        });
        settingsButton.setToolTipText("Settings");
        buttonPanel.add(settingsButton);
        
        final JButton tallyButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/calc24.png")));
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
        
        reportButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report24.png")));
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
        
        reportAllButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/report_all24.png")));
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
        
        offeringReportButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/offering24.png")));
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
        
        deleteButton = new JButton(new ImageIcon(FirstFruitsFrame.class.getResource("/icons/delete24.png")));
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

    private void updateRecentFileList()
    {
        recentFilesMenu.removeAll(); 
        final Deque<String> recentFiles = Settings.getInstance().getRecentFiles();
        for (final String recentFile : recentFiles) {
            final String fileName = recentFile.substring(recentFile.lastIndexOf(File.separatorChar) + 1);
            final JMenuItem menuItem = new JMenuItem(fileName);
            menuItem.addActionListener(new TextAction(fileName) {
                @Override
                public void actionPerformed(ActionEvent event) {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            final List<GivingRecord> records = new ArrayList<GivingRecord>();
                            boolean recordsLoaded = false;
                            try {
                                final File f = new File(recentFile);
                                final String password = GivingRecordsReader.getFilePassword(f);
                                boolean shouldLoadRecords = promptForPassword(f, password);
                                if (shouldLoadRecords) {
                                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                    loadFileRecords(records, f, password);
                                    recordsLoaded = true;
                                    currentPassword = password;
                                }
                            } catch (FileException e) {
                                JOptionPane.showMessageDialog(FirstFruitsFrame.this, e.getMessage(), "Error Loading File", JOptionPane.ERROR_MESSAGE);
                            } catch (FileNotFoundException e) {
                                showRemoveFromRecentList(menuItem, recentFile);
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(FirstFruitsFrame.this, e.getMessage(), "Error Loading File", JOptionPane.ERROR_MESSAGE);
                            } finally {
                                setCursor(Cursor.getDefaultCursor());
                            }
                            
                            if (recordsLoaded) {
                                Settings.getInstance().addRecentFile(recentFile);
                                currentFile = new File(recentFile);
                                passwordProtectItem.setEnabled(true);
                                RecordManager.getInstance().setRecords(records);
                            }
                        }
                    });
                }
            });
            menuItem.setToolTipText(recentFile);
            menuItem.setName(recentFile);
            recentFilesMenu.add(menuItem);
        }
    }
    
    private void showRemoveFromRecentList(JMenuItem menuItem, String fileName)
    {
        final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                        "The specified file no longer exists. Would you like to remove it from the list?", "File Not Found", 
                        JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            recentFilesMenu.remove(menuItem);
            Settings.getInstance().removeRecentFile(fileName);
        }
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
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                RecordManager.getInstance().createNew();
            }
        } else {
            RecordManager.getInstance().createNew();
        }
        currentFile = null;
        currentPassword = "";
        passwordProtectItem.setEnabled(false);
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
                    settingsDialog.updateCategoryList();
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
                } catch (Throwable e) {
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
    
    private void openFile() throws IOException, FileException
    {
        if (!confirmUnsavedChanges()) {
            return;
        }
            
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CsvFileFilter());
        fileChooser.setMultiSelectionEnabled(true);
        final List<GivingRecord> records = new ArrayList<GivingRecord>();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            final File[] selectedFiles = fileChooser.getSelectedFiles();
            
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String filePassword = "";
            boolean recordsLoaded = false;
            for (File f : selectedFiles) {
                Settings.getInstance().addRecentFile(f.getAbsolutePath());
                final String password = GivingRecordsReader.getFilePassword(f);
                boolean shouldLoadRecords = promptForPassword(f, password);
                if (shouldLoadRecords) {
                    loadFileRecords(records, f, password);
                    recordsLoaded = true;
                }
            }
            if (selectedFiles.length == 1) {
                currentFile = selectedFiles[0];
                passwordProtectItem.setEnabled(true);
                currentPassword = filePassword;
            } else {
                currentFile = null;
                passwordProtectItem.setEnabled(false);
                currentPassword = "";
            }
            if (recordsLoaded) {
                RecordManager.getInstance().setRecords(records);
            }
            setCursor(Cursor.getDefaultCursor());
        }
    }
        
    private boolean promptForPassword(File f, String password) 
    {
        if (password.isEmpty()) {
            return true;
        }
        boolean passwordCorrect = false;
        final FilePasswordPromptPanel fpp = new FilePasswordPromptPanel(f.getName(), password.toCharArray()); 
        while (!passwordCorrect) {
            final Icon lockIcon = new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock32.png"));
            int answer = JOptionPane.showConfirmDialog(  
                            null, fpp, String.format("Password for %s", f.getName()), JOptionPane.OK_CANCEL_OPTION,  
                            JOptionPane.PLAIN_MESSAGE,
                            lockIcon);  
            if (answer == JOptionPane.OK_OPTION)  
            {  
                if (fpp.checkPasswordInput()) {
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, fpp.getError(), "Password Failure", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Canceled so don't read records
                return false;
            }
        }
        return false;
    }

    private void loadFileRecords(final List<GivingRecord> records, File f, String password) throws IOException, FileException
    {
        try {
            final CategoryComparison categoryComparison = GivingRecordsReader.getCategoryComparison(f, !password.isEmpty());
            if (categoryComparison.hasExtraCategories()) {
                final StringBuilder confirmMessage = new StringBuilder();
                confirmMessage.append("<HTML>The records being imported do not match the categories defined in the program settings.<BR>");
                confirmMessage.append("The records contain the following categories:<BR>");
                confirmMessage.append("<B>[" + StringUtils.join(categoryComparison.allCategories, ", ") + "]</B><BR>");
                
                if (categoryComparison.missingCategories.size() > 0) {
                    confirmMessage.append("<BR>The records do not contain the following categories ");
                    confirmMessage.append("(<I>values will be zero</I>):<BR>");
                    confirmMessage.append("<B>[" + StringUtils.join(categoryComparison.missingCategories, ", ") + "]</B><BR>");
                }
                
                confirmMessage.append("<BR>Do you want to add these categories to the settings?  If you choose 'No', data for these<BR>");
                confirmMessage.append("categories will be ignored:<BR>");
                confirmMessage.append("<B>[" + StringUtils.join(categoryComparison.extraCategories, ", ") + "]</B><BR>");
                
                final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                                confirmMessage.toString(),
                                "Category Conflicts Detected", 
                                JOptionPane.YES_NO_CANCEL_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    for (String category : categoryComparison.extraCategories) {
                        Settings.getInstance().addCategory(category);
                    }
                    records.addAll(GivingRecordsReader.readRecordsFromFile(f, !password.isEmpty()));
                } else if (choice == JOptionPane.NO_OPTION) {
                    records.addAll(GivingRecordsReader.readRecordsFromFile(f, !password.isEmpty()));
                }
            } else {
                records.addAll(GivingRecordsReader.readRecordsFromFile(f, !password.isEmpty()));
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(FirstFruitsFrame.this, 
                            "Error occurred while loading invalid record file.\n" + f.getAbsolutePath() +
                            "\nline: " + e.getMessage(), 
                            "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean confirmUnsavedChanges()
    {
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(FirstFruitsFrame.this, 
                            "Unsaved changes exist. Do you want to save?", "Save Changes", 
                            JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    saveFile();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else if (choice == JOptionPane.CANCEL_OPTION ||
                       choice == JOptionPane.CLOSED_OPTION) {
                return false;
            }
        }
        return true;
    }
    
    private void saveAs() throws Throwable
    {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new CsvFileFilter()); 
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            if (FileUtils.getExtension(currentFile) == null) {
                currentFile = new File(currentFile.getAbsolutePath().concat("." + FileUtils.CSV));
            }
            passwordProtectItem.setEnabled(true);
            Settings.getInstance().addRecentFile(currentFile.getAbsolutePath());
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            GivingRecordsWriter.writeRecordsToFile(currentFile, currentPassword);
            setCursor(Cursor.getDefaultCursor());
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }
    
    private boolean setPasswordProtection() throws IOException
    {
        boolean passwordUpdatesMade = false;
        boolean finished = false;
        final FilePasswordPanel fpp = new FilePasswordPanel(currentPassword.toCharArray()); 
        while (!finished) {
            fpp.revalidate();
            fpp.repaint();
            final Icon lockEditIcon = new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock_edit32.png"));
            int answer = JOptionPane.showConfirmDialog(  
                            this, fpp, "File Password", JOptionPane.OK_CANCEL_OPTION,  
                            JOptionPane.PLAIN_MESSAGE,
                            lockEditIcon);
            if (answer == JOptionPane.OK_OPTION)  
            {  
                if (fpp.checkPasswordInput()) {
                    if (!fpp.getNewPassword().isEmpty() && fpp.isChangingPassword()) {
                        final Icon lockIcon = new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock_edit32.png"));
                        JOptionPane.showMessageDialog(this, "Password successfully changed.", "File Password", JOptionPane.INFORMATION_MESSAGE, lockIcon);
                    } else if (!fpp.getNewPassword().isEmpty()) {
                        final Icon lockIcon = new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock_add32.png"));
                        JOptionPane.showMessageDialog(this, "The file is now password protected.", "File Password", JOptionPane.INFORMATION_MESSAGE, lockIcon);
                    } else {
                        final Icon lockIcon = new ImageIcon(FirstFruitsFrame.class.getResource("/icons/lock_open32.png"));
                        JOptionPane.showMessageDialog(this, "The file is no longer password protected.", "File Password", JOptionPane.INFORMATION_MESSAGE, lockIcon);
                    }
                    currentPassword = fpp.getNewPassword();
                    passwordUpdatesMade = true;
                    finished = true;
                } else {
                    JOptionPane.showMessageDialog(null, fpp.getError(), "Password Failure", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                finished = true;
            }
        }
        return passwordUpdatesMade;
    }
    
    private void saveFile() throws Throwable 
    {
        if (currentFile != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            GivingRecordsWriter.writeRecordsToFile(currentFile, currentPassword);
            setCursor(Cursor.getDefaultCursor());
        } else {
            saveAs();
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }

    @Override
    public void update(Observable o, Object value)
    {
        if (o instanceof Settings) {
            updateRecentFileList();
        } else {
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
}
