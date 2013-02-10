package com.wardware.givingtracker.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.michaelbaranov.microba.calendar.DatePicker;
import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.Settings;

public class InputPanel extends JPanel implements Observer
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private AutoComboBox nameCombo;
    private List<CategoryInputPair> categoryInputs;
    private DatePicker picker;
    private NumberFormat simpleCurrencyFormat;
    private MyKeyListener keyListener;

    public InputPanel()
    {
        initComponents();
        RecordManager.getInstance().addObserver(this);
        Settings.getInstance().addObserver(this);
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
        final String date = SDF.format(picker.getDate());
        RecordManager.getInstance().setSelectedDate(date);
        picker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                final String date = SDF.format(picker.getDate());
                RecordManager.getInstance().setSelectedDate(date);
                final GivingRecord selectedRecord = RecordManager.getInstance().getSelectedRecord();
                if (selectedRecord != null) {
                    selectedRecord.setDate(date);
                    RecordManager.getInstance().setSelectedRecord(selectedRecord);
                }
            }
        });
        c.gridx = 1;
        c.gridwidth = 3;
        add(picker, c);
        
        c.gridwidth = 3;
        c.gridx = 1;
        c.gridy = 1;

        final JLabel nameLabel = new JLabel("Name");
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        add(nameLabel, c);

        nameCombo = new AutoComboBox(new ArrayList<String>());
        nameCombo.setMinimumSize(new Dimension(150, nameCombo.getHeight()));
        nameCombo.setCaseSensitive(false);
        nameCombo.setStrict(false);
        c.gridx = 1;
        c.gridwidth = 3;
        add(nameCombo, c);

        simpleCurrencyFormat = NumberFormat.getNumberInstance();
        simpleCurrencyFormat.setMaximumFractionDigits(2);
        simpleCurrencyFormat.setMinimumFractionDigits(2);

        keyListener = new MyKeyListener();

        categoryInputs = new ArrayList<CategoryInputPair>();
        final List<String> categories = Settings.getInstance().getCategories();
        updateCategeries(categories);
    }

    private class CategoryInputPair
    {
        private JLabel label;
        private JFormattedTextField inputField;

        public CategoryInputPair(JLabel label, JFormattedTextField inputField)
        {
            this.label = label;
            this.inputField = inputField;
        }

        public JLabel getLabel()
        {
            return label;
        }

        public JFormattedTextField getInputField()
        {
            return inputField;
        }
    }

    class MyKeyListener extends KeyAdapter
    {
        @Override
        public void keyReleased(KeyEvent event)
        {
            if (KeyEvent.VK_ENTER == event.getKeyCode()) {
                createOrUpdateRecord();
            }
            if (KeyEvent.VK_DOWN == event.getKeyCode()) {
                final CategoryInputPair[] inputs = new CategoryInputPair[categoryInputs.size()];
                categoryInputs.toArray(inputs);
                for (int i = 0; i < inputs.length; i++) {
                    if (inputs[i].getInputField().hasFocus()) {
                        if (i < inputs.length - 1) {
                            inputs[i+1].getInputField().requestFocusInWindow();
                        } else {
                            inputs[0].getInputField().requestFocusInWindow();
                        }
                        break;
                    }
                }
            }
            if (KeyEvent.VK_UP == event.getKeyCode()) {
                final CategoryInputPair[] inputs = new CategoryInputPair[categoryInputs.size()];
                categoryInputs.toArray(inputs);
                for (int i = 0; i < inputs.length; i++) {
                    if (inputs[i].getInputField().hasFocus()) {
                        if (i == 0) {
                            inputs[inputs.length-1].getInputField().requestFocusInWindow();
                        } else {
                            inputs[i-1].getInputField().requestFocusInWindow();
                        }
                        break;
                    }
                }
            }
        }
    }

    public void updateNames(List<String> names)
    {
        Collections.sort(names);
        nameCombo.setDataList(names);
    }

    public void updateCategeries(List<String> categories)
    {
        for (CategoryInputPair input : categoryInputs)
        {
            this.remove(input.getLabel());
            this.remove(input.getInputField());
        }
        categoryInputs.clear();

        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        int gridy = 3;
        c.gridx = 0;
        c.gridwidth = 1;
        for (String category : categories) {
            final JLabel categoryLabel = new JLabel(category);
            categoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            c.gridx = 0;
            c.gridy = gridy++;
            c.gridwidth = 1;
            add(categoryLabel, c);

            final JFormattedTextField valueField = new CurrencyFormattedTextField();
            valueField.setMinimumSize(new Dimension(150, valueField.getHeight()));
            valueField.addKeyListener(keyListener);
            c.gridx = 1;
            c.gridwidth = 3;
            add(valueField, c);

            categoryInputs.add(new CategoryInputPair(categoryLabel, valueField));
        }
        invalidate();
        updateUI();
    }

    private void createOrUpdateRecord()
    {
        if (nameCombo.getSelectedItem().toString().trim().isEmpty()) {
            return;
        }

        final GivingRecord record = new GivingRecord();
        record.setDate(SDF.format(picker.getDate()));
        record.setName(nameCombo.getSelectedItem().toString());

        for (CategoryInputPair input : categoryInputs) {
            final String category = input.getLabel().getText();
            final String value = input.getInputField().getText();

            try {
                if (value != null && !value.isEmpty()) {
                    final Double amount = simpleCurrencyFormat.parse(value).doubleValue();
                    record.setAmountForCategory(category, amount);
                }
            } catch (ParseException e) {
                // Invalid entry
            }
        }
        RecordManager.getInstance().updateRecord(record);
        nameCombo.requestFocusInWindow();
        nameCombo.setSelectedIndex(0);
        for (CategoryInputPair input : categoryInputs) {
            input.getInputField().setValue(null);
        }
    }

    @Override
    public void update(Observable o, Object value)
    {
        if (o instanceof RecordManager)
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

                for (CategoryInputPair input : categoryInputs) {
                    final String category = input.getLabel().getText();
                    input.getInputField().setValue(selectedRecord.getAmountForCategory(category));
                }
            } else {
                nameCombo.setSelectedIndex(0);
                for (CategoryInputPair input : categoryInputs) {
                    input.getInputField().setValue(null);
                }
            }
        } else if (o instanceof Settings) {
            final List<String> categories = Settings.getInstance().getCategories();
            updateCategeries(categories);
        }
    }
}
