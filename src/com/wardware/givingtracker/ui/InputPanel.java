package com.wardware.givingtracker.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
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
    private JPanel contentPanel;

    public InputPanel()
    {
        initComponents();
        RecordManager.getInstance().addObserver(this);
        Settings.getInstance().addObserver(this);
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
        
        contentPanel = new JPanel(new GridBagLayout());
        final JLabel dateLabel = new JLabel("Date");
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        contentPanel.add(dateLabel, Gbc.xyi(0, 0, 2).top(50).east());
        
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
        contentPanel.add(picker, Gbc.xyi(1, 0, 2).top(50).horizontal());
        
        final JLabel nameLabel = new JLabel("Name");
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        contentPanel.add(nameLabel, Gbc.xyi(0, 2, 2).east());

        nameCombo = new AutoComboBox(new ArrayList<String>());
        nameCombo.setMinimumSize(new Dimension(150, nameCombo.getHeight()));
        nameCombo.setCaseSensitive(false);
        nameCombo.setStrict(false);
        contentPanel.add(nameCombo, Gbc.xyi(1, 2, 2).horizontal());

        simpleCurrencyFormat = NumberFormat.getNumberInstance();
        simpleCurrencyFormat.setMaximumFractionDigits(2);
        simpleCurrencyFormat.setMinimumFractionDigits(2);

        keyListener = new MyKeyListener();

        categoryInputs = new ArrayList<CategoryInputPair>();
        updateCategeries();
        add(contentPanel, BorderLayout.NORTH);
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

    public void updateCategeries()
    {
        final List<String> categories = Settings.getInstance().getCategories();
        for (CategoryInputPair input : categoryInputs)
        {
            this.remove(input.getLabel());
            this.remove(input.getInputField());
        }
        categoryInputs.clear();

        int gridy = 3;
        for (String category : categories) {
            final JLabel categoryLabel = new JLabel(category);
            categoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contentPanel.add(categoryLabel, Gbc.xyi(0, gridy, 2).left(5).east());

            final JFormattedTextField valueField = new CurrencyFormattedTextField();
            valueField.addKeyListener(keyListener);
            contentPanel.add(valueField, Gbc.xyi(1, gridy, 2).horizontal());

            categoryInputs.add(new CategoryInputPair(categoryLabel, valueField));
            gridy++;
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
                System.err.println("couldn't parse input value: " + value);
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
            updateCategeries();
        }
    }
}
