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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.michaelbaranov.microba.calendar.DatePicker;
import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.Settings;

public class InputPanel extends JPanel implements Observer
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private AutoComboBox nameCombo;
    private List<Pair<JLabel, JFormattedTextField>> categoryInputs;
    private JFormattedTextField generalField;
    private JFormattedTextField missionsField;
    private JFormattedTextField buildingField;
    private DatePicker picker;
    private NumberFormat simpleCurrencyFormat;
    private SelectAllFocusListener focusListener;
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
        picker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
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
        nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        c.gridx = 0;
        c.gridy = 1;
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

        focusListener = new SelectAllFocusListener();
        keyListener = new MyKeyListener();

        categoryInputs = new ArrayList<Pair<JLabel, JFormattedTextField>>();
        final List<String> categories = Settings.getInstance().getCategories();
        updateCategeries(categories);
    }

    private class SelectAllFocusListener extends FocusAdapter
    {
        @Override
        public void focusGained(final FocusEvent event)
        {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
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
        public void keyReleased(KeyEvent event)
        {
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

    public void updateCategeries(List<String> categories)
    {
        for ( Pair<JLabel, JFormattedTextField> component : categoryInputs)
        {
            this.remove(component.getKey());
            this.remove(component.getValue());
        }
        categoryInputs.clear();
        
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.BOTH;
        int gridy = 2;
        c.gridx = 0;
        c.gridwidth = 1;
        for (String category : categories) {
            final JLabel categoryLabel = new JLabel(category);
            categoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            c.gridx = 0;
            c.gridy = gridy++;
            c.gridwidth = 1;
            add(categoryLabel, c);

            final JFormattedTextField valueField = new JFormattedTextField(simpleCurrencyFormat);
            valueField.setMinimumSize(new Dimension(150, valueField.getHeight()));
            valueField.addKeyListener(keyListener);
            valueField.addFocusListener(focusListener);
            c.gridx = 1;
            c.gridwidth = 3;
            add(valueField, c);

            categoryInputs.add(new ImmutablePair<JLabel, JFormattedTextField>(categoryLabel, valueField));
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

        try {
            for (Pair<JLabel, JFormattedTextField> categoryPair : categoryInputs) {
                final String category = categoryPair.getKey().getText();
                final Double amount = simpleCurrencyFormat.parse(categoryPair.getValue().getText()).doubleValue();
                record.setAmountForCategory(category, amount);
            }
        } catch (ParseException e) {
            return;
        }
        RecordManager.getInstance().updateRecord(record);
        nameCombo.requestFocusInWindow();
        nameCombo.setSelectedIndex(0);
        for (Pair<JLabel, JFormattedTextField> categoryCombo : categoryInputs) {
            categoryCombo.getValue().setValue(null);
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

                for (Pair<JLabel, JFormattedTextField> categoryCombo : categoryInputs) {
                    final String category = categoryCombo.getKey().getText();
                    categoryCombo.getValue().setValue(selectedRecord.getAmountForCategory(category));
                }
            } else {
                nameCombo.setSelectedIndex(0);
                for (Pair<JLabel, JFormattedTextField> categoryCombo : categoryInputs) {
                    categoryCombo.getValue().setValue(null);
                }
            }
        } else if (o instanceof Settings) {
            final List<String> categories = Settings.getInstance().getCategories();
            updateCategeries(categories);
        }
    }
}
