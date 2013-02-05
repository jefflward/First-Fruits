package com.wardware.givingtracker.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.michaelbaranov.microba.calendar.DatePicker;
import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;

public class InputPanel extends JPanel implements Observer
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private AutoComboBox nameCombo;
    private JFormattedTextField generalField;
    private JFormattedTextField missionsField;
    private JFormattedTextField buildingField;
    private DatePicker picker;
    private NumberFormat simpleCurrencyFormat;
    
    public InputPanel()
    {
        initComponents();
        RecordManager.getInstance().addObserver(this);
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        final JLabel dateLabel = new JLabel("Date");
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(dateLabel, c);
        
        picker = new DatePicker(new Date());
        picker.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final GivingRecord selectedRecord = RecordManager.getInstance().getSelectedRecord();
                if (selectedRecord != null) {
                    selectedRecord.setDate(SDF.format(picker.getDate()));
                    RecordManager.getInstance().setSelectedRecord(selectedRecord);
                }
            }
        });
        c.gridx = 1;
        c.gridwidth = 3; 
        add(picker, c);
        
        final JLabel nameLabel = new JLabel("Name");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1; 
        add(nameLabel, c);
        final JLabel generalLabel = new JLabel("General");
        c.gridx = 1;
        add(generalLabel, c);
        final JLabel missionsLabel = new JLabel("Missions");
        c.gridx = 2;
        add(missionsLabel, c);
        final JLabel otherLabel = new JLabel("Building");
        c.gridx = 3;
        add(otherLabel, c);
   
        c.gridx = 0;
        c.gridy = 2;
        nameCombo = new AutoComboBox(new ArrayList<String>());
        nameCombo.setMinimumSize(new Dimension(150, nameCombo.getHeight()));
        nameCombo.setCaseSensitive(false);
        nameCombo.setStrict(false);
        add(nameCombo, c);
        
        simpleCurrencyFormat = NumberFormat.getNumberInstance();
        simpleCurrencyFormat.setMaximumFractionDigits(2);
        
        final SelectAllFocusListener focusListener = new SelectAllFocusListener();
        final MyKeyListener keyListener = new MyKeyListener();
        generalField = new JFormattedTextField(simpleCurrencyFormat);
        generalField.addKeyListener(keyListener);
        generalField.addFocusListener(focusListener);
        c.gridx = 1;
        add(generalField, c);
        missionsField = new JFormattedTextField(simpleCurrencyFormat);
        missionsField.addKeyListener(keyListener);
        missionsField.addFocusListener(focusListener);
        c.gridx = 2;
        add(missionsField, c);
        buildingField = new JFormattedTextField(simpleCurrencyFormat);
        buildingField.addKeyListener(keyListener);
        buildingField.addFocusListener(focusListener);
        c.gridx = 3;
        add(buildingField, c);
    }
    
    private class SelectAllFocusListener extends FocusAdapter
    {
        @Override
        public void focusGained(final FocusEvent event) {
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                public void run() {
                    final Component component = event.getComponent();
                    if (component instanceof JFormattedTextField) {
                        ((JFormattedTextField) component).selectAll();
                    }
                }
            });
        }
    }
    
    class MyKeyListener extends KeyAdapter
    {
        @Override
        public void keyReleased(KeyEvent event) {
            if (KeyEvent.VK_ENTER == event.getKeyCode()) {
                createOrUpdateRecord();
            }
            if (KeyEvent.VK_RIGHT == event.getKeyCode()) {
                if (generalField.hasFocus()) {
                    missionsField.requestFocusInWindow();
                } else if (missionsField.hasFocus()) {
                    buildingField.requestFocusInWindow();
                } else if (buildingField.hasFocus()) {
                    generalField.requestFocusInWindow();
                }
            }
        }
    }
    
    public void updateNames(List<String> names)
    {
        Collections.sort(names);
        nameCombo.setDataList(names);
    }
    
    private void createOrUpdateRecord()
    {
        if (nameCombo.getSelectedItem().toString().trim().isEmpty()) {
            return;
        }
            
        final GivingRecord record = new GivingRecord();
        record.setDate(SDF.format(picker.getDate()));
        record.setName(nameCombo.getSelectedItem().toString());
        try {
            if (!generalField.getText().isEmpty()) {
                record.setGeneral(simpleCurrencyFormat.parse(generalField.getText()).doubleValue());
            }
            if (!missionsField.getText().isEmpty()) {
                record.setMissions(simpleCurrencyFormat.parse(missionsField.getText()).doubleValue());
            }
            if (!buildingField.getText().isEmpty()) {
                record.setBuilding(simpleCurrencyFormat.parse(buildingField.getText()).doubleValue());
            }
            RecordManager.getInstance().updateRecord(record);
            nameCombo.requestFocusInWindow();
            nameCombo.setSelectedIndex(0);
            generalField.setValue(null);
            missionsField.setValue(null);
            buildingField.setValue(null);
        } catch (ParseException e) {
        }
    }
    
    @Override
    public void update(Observable o, Object value)
    {
        updateNames(RecordManager.getInstance().getUniqueNames());
        final GivingRecord selectedRecord = RecordManager.getInstance().getSelectedRecord();
        if (selectedRecord != null) {
            try {
                picker.setDate(SDF.parse(selectedRecord.getDate()));
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            nameCombo.setSelectedItem(selectedRecord.getName());
            generalField.setValue(selectedRecord.getGeneral());
            missionsField.setValue(selectedRecord.getMissions());
            buildingField.setValue(selectedRecord.getBuilding());
        } else {
            nameCombo.setSelectedIndex(0);
            generalField.setText("");
            missionsField.setText("");
            buildingField.setText("");
        }
    }
}
