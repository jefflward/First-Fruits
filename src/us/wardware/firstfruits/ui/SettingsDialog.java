package us.wardware.firstfruits.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.TextAction;

import org.apache.commons.lang3.StringUtils;

import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;


public class SettingsDialog extends JDialog
{
    private JList optionsList;
    private JSplitPane splitPane;
    private GeneralPanel generalPanel;
    private CategoriesPanel categoriesPanel;
    private JPanel rightPanel;
    private JPanel buttonPanel;
    private boolean initialSetup;
    
    private List<JPanel> steps;
    private WelcomePanel welcomePanel;
    
    private static final String WELCOME = "Welcome";
    private static final String GENERAL = "General";
    private static final String CATEGORIES = "Categories";

    public SettingsDialog(JFrame parent, boolean initialSetup)
    {
        super(parent);
        this.initialSetup = initialSetup;
        initComponents();
        optionsList.setSelectedIndex(0);
        setLocationRelativeTo(parent);
    }
    
    private class WelcomePanel extends JPanel
    {
        public WelcomePanel()
        {
            this.setLayout(new BorderLayout());
            
            ImageIcon imageIcon = new ImageIcon(WelcomePanel.class.getResource("/icons/logo48.png"));
            
            final JPanel top = new JPanel(new GridBagLayout());
            final JLabel logo = new JLabel(imageIcon);
            top.add(logo, Gbc.xyi(0,0,10));
            top.add(new JLabel("Welcome to First Fruits!"), Gbc.xyi(1,0,5).horizontal());
            final JLabel welcomeLabel = new JLabel("<HTML>Please take some time to define the program settings." +
            		"<BR><BR><B>NOTE:</B> These settings can be updated later by choosing settings from the <B>File</B> menu.</HTML>");
            top.add(welcomeLabel, Gbc.xyi(0,1,5).horizontal().gridWidth(2).top(20));
            add(top, BorderLayout.NORTH);
        }
    }
    
    private class GeneralPanel extends JPanel
    {
        private JPanel contents;
        
        private Map<String, JTextField> inputMap = new HashMap<String, JTextField>();

        public GeneralPanel()
        {
            this.setLayout(new BorderLayout());
            contents = new JPanel(new GridBagLayout());
            contents.setBorder(new TitledBorder("Church Address"));
            addNamedInputFields(Settings.CHURCH_NAME_KEY, "Name", 0);
            addNamedInputFields(Settings.ADDRESS1, "Address 1", 1);
            addNamedInputFields(Settings.ADDRESS2, "Address 2", 2);
            addNamedInputFields(Settings.CITY, "City", 3);
            addNamedInputFields(Settings.STATE, "State", 4);
            addNamedInputFields(Settings.ZIP, "Zip", 5);
            addNamedInputFields(Settings.PHONE, "Phone", 6);
            add(contents, BorderLayout.NORTH);
        }
        
        private void addNamedInputFields(String settingsKey, String text, int y)
        {
            final JLabel label = new JLabel(text);
            label.setHorizontalAlignment(JLabel.RIGHT);
            contents.add(label, Gbc.xyi(0, y, 2).east());
            
            final JTextField valueField = new JTextField(Settings.getInstance().getStringValue(settingsKey));
            contents.add(valueField, Gbc.xyi(1, y, 2).horizontal());
            inputMap.put(settingsKey, valueField);
        }
        
        public void saveSettings()
        {
            for (String key : inputMap.keySet()) {
                Settings.getInstance().setStringValue(key, inputMap.get(key).getText().trim());
            }
        }
    }
    
    private class CategoriesPanel extends JPanel
    {
        private JList list;
        private DefaultListModel listModel;
        private JTextField categoryName;
        private JButton removeButton;
        private JButton addButton;

        public CategoriesPanel()
        {
            this.setLayout(new BorderLayout());
            this.setBorder(new TitledBorder("Category Settings"));
            
            final JLabel categorySettingsHelp = new JLabel("<HTML>The categories defined here will be used throughout the application." +
            		"<BR>The categories will be displayed in the order specified below." +
            		"<BR><BR><B>Note:</B> To re-order categories simply drag and drop in the desired order.</HTML>");
            add(categorySettingsHelp, BorderLayout.NORTH);
            
            listModel = new DefaultListModel();
            
            updateCategories();
            
            list = new JList(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectionModel(new DefaultListSelectionModel() {
                @Override
                public void setSelectionInterval(int index0, int index1) {
                    if (list.isSelectedIndex(index0)) {
                        list.clearSelection();
                        removeButton.setEnabled(false);
                        addButton.setText("Add");
                    } else {
                        list.clearSelection();
                        list.addSelectionInterval(index0, index1);
                        removeButton.setEnabled(true);
                        addButton.setText("Rename");
                    }
                }
            });
            list.setDropMode(DropMode.INSERT);
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent event) {
                    if (KeyEvent.VK_DELETE == event.getKeyCode()) {
                        removeCategories();
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(list);
            
            list.setDragEnabled(true);
            list.setTransferHandler(new ListTransferHandler());
            add(scrollPane, BorderLayout.CENTER);
            
            final JPanel inputPanel = new JPanel(new GridBagLayout());
            categoryName = new JTextField();
            categoryName.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent event) {
                    if (KeyEvent.VK_ENTER == event.getKeyCode()) {
                        addOrRenameCategory();
                    }
                }
            });
            inputPanel.add(categoryName, Gbc.xyi(0, 0, 2).horizontal());
            addButton = new JButton(new TextAction("Add"){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    addOrRenameCategory();
                }
            });
            addButton.setDefaultCapable(true);
            inputPanel.add(addButton, Gbc.xyi(1, 0, 2));
            removeButton = new JButton(new TextAction("Remove"){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    removeCategories();
                }
            });
            removeButton.setEnabled(false);
            inputPanel.add(removeButton, Gbc.xyi(2, 0, 2));
            
            add(inputPanel, BorderLayout.SOUTH);
        }

        private void updateCategories()
        {
            listModel.clear();
            final List<String> categories = Settings.getInstance().getCategories();
            for (String category : categories) {
                listModel.addElement(category.trim());
            }
        }
        
        private void addOrRenameCategory()
        {
            final String category = categoryName.getText().trim();
            if (!category.isEmpty()) {
                final String selectedCategory = (String) list.getSelectedValue();
                if (selectedCategory != null && !selectedCategory.equals(category) ) {
                    String additionalNote = "";
                    if (RecordManager.getInstance().hasRecords()) {
                        additionalNote = "<BR><BR><B>NOTE:</B> This will be applied to records for the currently open database.";
                    }
                    final String message = String.format("<HTML>Are you sure you want to rename category '%s' to '%s'?%s</HTML>", selectedCategory, category, additionalNote);
                    if (JOptionPane.showConfirmDialog(SettingsDialog.this, message, "Rename Category", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                        RecordManager.getInstance().renameCategory(selectedCategory, category);
                        categoryName.setText("");
                        listModel.setElementAt(category, list.getSelectedIndex());
                    }
                } else {
                    final List<String> currentCategories = getCategories();
                    if (!currentCategories.contains(category)) {
                        listModel.addElement(category);
                    }
                    categoryName.setText("");
                }
                saveSettings();
            }
        }
        
        private void removeCategories()
        {
            final String selectedCategory = (String) list.getSelectedValue();
            if (selectedCategory != null) {
                final String message = String.format("<HTML>Data may exist for category '%s'. Are you sure you want to remove this category?</HTML>", selectedCategory);
                if (JOptionPane.showConfirmDialog(SettingsDialog.this, message, "Remove Category", 
                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                    listModel.removeElement(selectedCategory);
                    RecordManager.getInstance().removeCategory(selectedCategory);
                    categoryName.setText("");
                    addButton.setText("Add");
                }
                saveSettings();
            }
        }
        
        public List<String> getCategories()
        {
            final List<String> categories = new ArrayList<String>();
            final Object[] items = listModel.toArray();
            for (Object item : items) {
                categories.add(item.toString());
            }
            return categories;
        }

        public void saveSettings()
        {
            Settings.getInstance().setStringValue(Settings.CATEGORIES_KEY, StringUtils.join(getCategories(), ";"));
        }
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("First Fruits Settings");
        if (initialSetup) {
            setTitle("Initial Setup");
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 375));
        
        steps = new ArrayList<JPanel>();
        splitPane = new JSplitPane(); 
        
        welcomePanel = new WelcomePanel();
        generalPanel = new GeneralPanel();
        categoriesPanel = new CategoriesPanel();
        
        steps.add(welcomePanel);
        steps.add(generalPanel);
        steps.add(categoriesPanel);
      
        optionsList = new JList(new Object[]{GENERAL, CATEGORIES});
        if (initialSetup) {
            optionsList = new JList(new Object[]{WELCOME, GENERAL, CATEGORIES});
        }
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                final Object selection = optionsList.getSelectedValue();
                rightPanel.removeAll();
                if (WELCOME.equals(selection)) {
                    rightPanel.add(welcomePanel, BorderLayout.NORTH);
                } else if (GENERAL.equals(selection)) {
                    rightPanel.add(generalPanel, BorderLayout.NORTH);
                } else if (CATEGORIES.equals(selection)) {
                    rightPanel.add(categoriesPanel, BorderLayout.CENTER);
                }
                rightPanel.invalidate();
                rightPanel.updateUI();
            }
        });
        
        final JScrollPane scrollPane = new JScrollPane(optionsList);
        splitPane.setLeftComponent(scrollPane);
        
        if (initialSetup) {
            createGuideButtonPanel();
        } else {
            createDefaultButtonPanel();
        }
        
        rightPanel = new JPanel(new BorderLayout());
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(100);
        
        add(splitPane, BorderLayout.CENTER);
        final JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(buttonPanel, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        pack();
    }
    
    private void createGuideButtonPanel()
    {
        buttonPanel = new JPanel();
        
        final JButton backButton = new JButton();
        final JButton nextButton = new JButton();
        backButton.setAction(new TextAction("Back"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int step = optionsList.getSelectedIndex();
                int previous = step-1;
                if (previous <= 0) {
                    backButton.setEnabled(false);
                    previous = 0;
                }
                nextButton.setEnabled(true);
                optionsList.setSelectedIndex(previous);
            }
        });
        backButton.setEnabled(false);
        buttonPanel.add(backButton);
        
        nextButton.setAction(new TextAction("Next"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                int step = optionsList.getSelectedIndex();
                int next = step+1;
                if (next >= optionsList.getModel().getSize()-1) {
                    nextButton.setEnabled(false);
                    next = optionsList.getModel().getSize()-1;
                }
                backButton.setEnabled(true);
                optionsList.setSelectedIndex(next);
            }
        });
        buttonPanel.add(nextButton);
        
        JButton finishButton = new JButton(new TextAction("Finish"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                saveSettings();
                setVisible(false);
                dispose();
            }
        });
        finishButton.setDefaultCapable(true);
        
        buttonPanel.add(finishButton);
    }

    private void createDefaultButtonPanel()
    {
        buttonPanel = new JPanel();
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
    }
    
    private void saveSettings()
    {
        generalPanel.saveSettings();
        categoriesPanel.saveSettings();
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final SettingsDialog dialog = new SettingsDialog(null, true);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }

    public void updateCategoryList()
    {
        if (categoriesPanel != null) {
            categoriesPanel.updateCategories();
        }
    }
}
