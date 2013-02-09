package com.wardware.givingtracker.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.Settings;
import com.wardware.givingtracker.fileio.CsvFileFilter;
import com.wardware.givingtracker.fileio.FileUtils;

public class GivingTrackerFrame extends JFrame implements Observer
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
    private JCheckBox filterByDateCheckBox;
    
    public GivingTrackerFrame()
    {
        initComponents();
        RecordManager.getInstance().addObserver(this);
    }

    private void initComponents()
    {
        setTitle("Giving Tracker");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
        newItem.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/new.png")));
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
        open.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/open.png")));
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
        saveItem.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/save.png")));
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
        saveAsItem.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/save_as.png")));
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
        settings.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/settings.png")));
        settings.setMnemonic('E');
        fileMenu.add(settings);
        
        fileMenu.add(new JSeparator());
        
        final JMenuItem exit = new JMenuItem(new TextAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                handleExitConfirmation();
            }
        });
        exit.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/exit.png")));
        exit.setMnemonic('X');
        fileMenu.add(exit);
        
        menuBar.add(fileMenu);
        
        reportsMenu = new JMenu("Reports");
        reportsMenu.setMnemonic('R');
        reportsMenu.setEnabled(false);
        final JMenuItem reportItem = new JMenuItem(new TextAction("Create Report") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReport();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportItem.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/report.png")));
        reportItem.setMnemonic('S');
        reportsMenu.add(reportItem);
        
        final JMenuItem reportAll = new JMenuItem(new TextAction("Report All") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    runReportAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        reportAll.setIcon(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/report_all.png")));
        reportAll.setMnemonic('A');
        reportsMenu.add(reportAll);
        menuBar.add(reportsMenu);
        
        final JMenu helpMenu = new JMenu("Help");
        final JMenuItem about = new JMenuItem(new TextAction("About") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                about();
            }
        });
        helpMenu.add(about);
        menuBar.add(helpMenu); 
        setJMenuBar(menuBar);
        
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        inputPanel = new InputPanel();
        splitPane.setLeftComponent(inputPanel); 
        
        recordsTable = new RecordsTable();
        final JPanel right = new JPanel(new BorderLayout());
        final JPanel topRight = new JPanel(new BorderLayout());
        filterByDateCheckBox = new JCheckBox("Only display records for selected date");
        //filterByDateCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
        filterByDateCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                RecordManager.getInstance().setFilterByDate(filterByDateCheckBox.isSelected());
            }
        });
        topRight.add(filterByDateCheckBox, BorderLayout.EAST);
        right.add(topRight, BorderLayout.NORTH);
        final JScrollPane scrollPane = new JScrollPane(recordsTable);
        right.add(scrollPane, BorderLayout.CENTER);
        splitPane.setRightComponent(right);
        
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        
        final JButton newButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/new.png")));
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                createNew();
            }
        });
        newButton.setToolTipText("New");
        buttonPanel.add(newButton);
        
        final JButton openButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/open.png")));
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
        
        saveButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/save.png")));
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
        
        final JButton settingsButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/settings.png")));
        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                configureSettings();
            }
        });
        settingsButton.setToolTipText("Settings");
        buttonPanel.add(settingsButton);
        
        reportButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/report.png")));
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
        reportButton.setToolTipText("Create Report");
        reportButton.setEnabled(false);
        buttonPanel.add(reportButton);
        
        reportAllButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/report_all.png")));
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
        reportAllButton.setToolTipText("Report All");
        reportAllButton.setEnabled(false);
        buttonPanel.add(reportAllButton);
        
        deleteButton = new JButton(new ImageIcon(GivingTrackerFrame.class.getResource("/icons/delete.png")));
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
        JOptionPane.showMessageDialog(this, "Giving Tracker\n" +
                "Version: 0.1\n" +
                "Developed By: Jeff Ward\n" +
                "(c) Copyright Jeff Ward 2012, 2013.  All rights reserved.", "About Giving Tracker", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    protected void createNew()
    {
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(GivingTrackerFrame.this, 
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
        recordsTable.deleteSelectedRecords();
        if (currentFile == null && RecordManager.getInstance().getAllRecords().size() == 0) {
            RecordManager.getInstance().setUnsavedChanges(false);
        }
    }
    
    private void configureSettings()
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final SettingsDialog settings = new SettingsDialog();
                settings.setVisible(true);
            }
        });
    }
    
    private void runReportAll() throws IOException
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final ReportAllDialog dialog = new ReportAllDialog();
                dialog.setLocationRelativeTo(GivingTrackerFrame.this);
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
                final ReportDialog reportDialog = new ReportDialog();
                reportDialog.setLocationRelativeTo(GivingTrackerFrame.this);
                reportDialog.setVisible(true);
                reportDialog.setAlwaysOnTop(true);
            }
        });
    }
    
    private void handleExitConfirmation()
    {
        Settings.getInstance().saveSettings();
        if (RecordManager.getInstance().hasUnsavedChanges()) {
            final int choice = JOptionPane.showConfirmDialog(GivingTrackerFrame.this, 
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
            final int choice = JOptionPane.showConfirmDialog(GivingTrackerFrame.this, 
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
                records.addAll(getRecordsFromFile(f));
            }
        }
        RecordManager.getInstance().setRecords(records);
    }

    private Set<GivingRecord> getRecordsFromFile(File file) throws IOException
    {
        final Set<GivingRecord> records = new HashSet<GivingRecord>();
        final FileInputStream fstream = new FileInputStream(file);
        final DataInputStream in = new DataInputStream(fstream);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try 
        {
            final String firstLine = br.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                throw new RuntimeException(String.format("The file: %f is missing header.", file.getName()));
            }

            final String[] tokens = firstLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
            }

            // This is the old format
            String[] headers = {"Date","Name","General", "Missions", "Building", "Total"};
            if (tokens[0].equals("Date")) {
                headers = tokens;
            }

            String line;
            while ((line = br.readLine()) != null)   {
                try {
                    final GivingRecord record = GivingRecord.fromCsv(line, headers);
                    records.add(record);
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(GivingTrackerFrame.this, 
                                    "Error occurred while loading invalid record file.\n" + file.getAbsolutePath(), 
                                    "Load Error", JOptionPane.ERROR_MESSAGE);
                    break;
                }
            }
        } finally {
           in.close();
           br.close();
        }
        return records;
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
            writeRecordsToFile();
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }
    
    private void saveFile() throws IOException 
    {
        if (currentFile != null) {
            writeRecordsToFile();
        } else {
            saveAs();
        }
        RecordManager.getInstance().setUnsavedChanges(false);
    }

    private void writeRecordsToFile() throws IOException
    {
        final FileWriter fileWriter = new FileWriter(currentFile);
        final PrintWriter printWriter = new PrintWriter(fileWriter);
        final RecordTableModel tableModel = (RecordTableModel) recordsTable.getModel();
        final String[] columnNames = tableModel.getColumns();
        final StringBuilder columnsCsv = new StringBuilder();
        for (String column : columnNames) {
            columnsCsv.append(column);
            columnsCsv.append(",");
        }
        columnsCsv.deleteCharAt(columnsCsv.length() - 1);
        printWriter.println(columnsCsv.toString());    
        final List<GivingRecord> records = RecordManager.getInstance().getAllRecords();
        for (GivingRecord record : records) {
            printWriter.println(record.toCsv());
        }
        printWriter.flush();
        printWriter.close();
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
    }
}
