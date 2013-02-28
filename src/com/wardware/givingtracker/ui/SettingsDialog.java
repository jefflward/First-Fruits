package com.wardware.givingtracker.ui;

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
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
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

import com.wardware.givingtracker.Settings;

public class SettingsDialog extends JDialog
{
    private JList optionsList;
    private JSplitPane splitPane;
    private GeneralPanel generalPanel;
    private CategoriesPanel categoriesPanel;
    private JPanel rightPanel;
    private JPanel buttonPanel;
    
    private static final String GENERAL = "General";
    private static final String CATEGORIES = "Categories";

    public SettingsDialog(JFrame parent)
    {
        super(parent);
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private class GeneralPanel extends JPanel
    {
        private JPanel contents;
        
        private Map<String, JTextField> inputMap = new HashMap<String, JTextField>();

        public GeneralPanel()
        {
            this.setLayout(new BorderLayout());
            contents = new JPanel(new GridBagLayout());
            addNamedInputFields(Settings.ORGANIZATION_NAME_KEY, "Name", 0);
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

        public CategoriesPanel()
        {
            this.setLayout(new BorderLayout());
            this.setBorder(new TitledBorder("Category Settings"));
            
            final JLabel categorySettingsHelp = new JLabel("<HTML>The categories defined here will be used throughout the application." +
            		"<BR>The categories will be displayed in the order specified below." +
            		"<BR><BR><B>Note:</B> To re-order categories simply drag and drop in the desired order.</HTML>");
            add(categorySettingsHelp, BorderLayout.NORTH);
            
            listModel = new DefaultListModel();
            
            final List<String> categories = Settings.getInstance().getCategories();
            for (String category : categories) {
                listModel.addElement(category.trim());
            }
            
            list = new JList(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setDropMode(DropMode.INSERT);
            list.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent event) {
                    if (KeyEvent.VK_DELETE == event.getKeyCode()) {
                        if (!list.getSelectionModel().isSelectionEmpty()) {
                            listModel.remove(list.getSelectedIndex());
                        }
                    }
                }
            });
            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent arg0) {
                    removeButton.setEnabled(list.getSelectedIndices().length > 0);
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
                        addCategory();
                    }
                }
            });
            inputPanel.add(categoryName, Gbc.xyi(0, 0, 2).horizontal());
            final JButton addButton = new JButton(new TextAction("Add"){
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    addCategory();
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
        
        private void addCategory()
        {
            final String category = categoryName.getText().trim();
            if (!category.isEmpty()) {
                final List<String> currentCategories = getCategories();
                if (!currentCategories.contains(category)) {
                    listModel.addElement(category);
                }
            }
            categoryName.setText("");
        }
        
        private void removeCategories()
        {
            final int[] selectedIndices = list.getSelectedIndices();
            for (int i = selectedIndices.length-1; i >=0; i--) {
                listModel.removeElementAt(selectedIndices[i]);
            } 
            categoryName.setText("");
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
        setTitle("Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        
        splitPane = new JSplitPane(); 
        
        generalPanel = new GeneralPanel();
        categoriesPanel = new CategoriesPanel();
        
        optionsList = new JList(new Object[]{GENERAL, CATEGORIES});
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                final Object selection = optionsList.getSelectedValue();
                rightPanel.removeAll();
                if (GENERAL.equals(selection)) {
                    rightPanel.add(generalPanel, BorderLayout.NORTH);
                } else if (CATEGORIES.equals(selection)) {
                    rightPanel.add(categoriesPanel, BorderLayout.CENTER);
                }
                rightPanel.add(buttonPanel, BorderLayout.SOUTH);
                rightPanel.invalidate();
                rightPanel.updateUI();
                invalidate();
                pack();
            }
        });
        
        final JScrollPane scrollPane = new JScrollPane(optionsList);
        splitPane.setLeftComponent(scrollPane);
        
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
        
        rightPanel = new JPanel(new BorderLayout());
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(100);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(splitPane, BorderLayout.CENTER);
        pack();
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
                final SettingsDialog dialog = new SettingsDialog(null);
                dialog.setVisible(true);
            }
        });
    }
}
