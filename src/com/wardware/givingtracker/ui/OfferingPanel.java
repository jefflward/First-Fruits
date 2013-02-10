package com.wardware.givingtracker.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.jasperreports.engine.JRDataSource;

import com.michaelbaranov.microba.calendar.DatePicker;
import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;
import com.wardware.givingtracker.Settings;

public class OfferingPanel extends JPanel
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private Map<String, CategoryInputFields> categoryInputs;
    private DatePicker picker;
    private NumberFormat simpleCurrencyFormat;
    private List<GivingRecord> recordsForDate;
    private JFormattedTextField offeringTotalField;
    private JLabel offeringBalancesLabel;
    private Double totalForAllCategories;

    public OfferingPanel()
    {
        simpleCurrencyFormat = NumberFormat.getNumberInstance();
        simpleCurrencyFormat.setMaximumFractionDigits(2);
        simpleCurrencyFormat.setMinimumFractionDigits(2);

        totalForAllCategories = 0.0;

        initComponents();
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());

        final JPanel datePanel = new JPanel();

        final JLabel dateLabel = new JLabel("Offering Date");
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        datePanel.add(dateLabel);

        picker = new DatePicker(new Date());
        if (RecordManager.getInstance().getSelectedDate() != null) {
            try {
                picker.setDate(SDF.parse(RecordManager.getInstance().getSelectedDate()));
            } catch (PropertyVetoException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        final String date = SDF.format(picker.getDate());
        recordsForDate = RecordManager.getInstance().getRecordsForDate(date);

        picker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final String date = SDF.format(picker.getDate());
                recordsForDate = RecordManager.getInstance().getRecordsForDate(date);
                handleDateChange();
            }
        });

        datePanel.add(picker);
        add(datePanel, BorderLayout.NORTH);
        
        final JPanel bottomPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        bottomPanel.add(new JLabel());
        bottomPanel.add(new JLabel());
        bottomPanel.add(new JLabel("Offering Total"));
        offeringTotalField = new CurrencyFormattedTextField();
        offeringTotalField.setEditable(false);
        
        bottomPanel.add(offeringTotalField);
        offeringBalancesLabel = new JLabel();
        bottomPanel.add(offeringBalancesLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        final JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        final JPanel headerPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        headerPanel.add(new JLabel());
        final JLabel currencyLabel = new JLabel("Currency");
        currencyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(currencyLabel);
        final JLabel checksLabel = new JLabel("Checks");
        checksLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(checksLabel);
        final JLabel totalLabel = new JLabel("Total");
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        headerPanel.add(totalLabel);
        center.add(headerPanel);

        categoryInputs = new HashMap<String, CategoryInputFields>();
        final JPanel categoryPanel = new JPanel(new GridLayout(0, 5, 5, 5));
        addPanelForCategory(center, categoryPanel, "Uncategorized", false);
        final List<String> categories = Settings.getInstance().getCategories();
        for (String category : categories) {
            addPanelForCategory(center, categoryPanel, category, true);
        }
        add(center, BorderLayout.CENTER);

        invalidate();
        updateUI();
    }

    private void addPanelForCategory(final JPanel center, final JPanel categoryPanel, String category,
                    boolean checkBalance)
    {
        final CategoryInputFields fields = new CategoryInputFields(category, checkBalance);
        categoryPanel.add(fields.getCategoryNameLabel());
        categoryPanel.add(fields.getCurrencyField());
        categoryPanel.add(fields.getChecksField());
        categoryPanel.add(fields.getTotalField());
        categoryPanel.add(fields.getBalancesLabel());
        center.add(categoryPanel);
        categoryInputs.put(category, fields);
    }

    private void handleDateChange()
    {
        for (String category : categoryInputs.keySet()) {
            final CategoryInputFields fields = categoryInputs.get(category);
            fields.updateTotal();
        }
        offeringTotalField.setText("");
    }

    private class CategoryInputFields
    {
        private JLabel categoryNameLabel;
        private JFormattedTextField currencyField;
        private JFormattedTextField checksField;
        private JFormattedTextField totalField;
        private JLabel balancesLabel;
        private boolean balances;

        private String categoryName;
        private boolean checkBalance;

        public CategoryInputFields(String categoryName, boolean checkBalance)
        {
            this.categoryName = categoryName;
            this.checkBalance = checkBalance;
            categoryNameLabel = new JLabel(categoryName);
            categoryNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            final PropertyChangeListener valueChangedListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    final Object newValue = evt.getNewValue();
                    final Object oldValue = evt.getOldValue();
                    if (newValue != null || oldValue != null) {
                        updateBalances();
                    }
                }
            };

            currencyField = new CurrencyFormattedTextField();
            currencyField.addPropertyChangeListener("value", valueChangedListener);
            currencyField.setMinimumSize(new Dimension(150, currencyField.getHeight()));
            checksField = new CurrencyFormattedTextField();
            checksField.addPropertyChangeListener("value", valueChangedListener);
            checksField.setMinimumSize(new Dimension(150, checksField.getHeight()));
            totalField = new CurrencyFormattedTextField();
            totalField.setMinimumSize(new Dimension(150, totalField.getHeight()));
            totalField.setEditable(false);
            balancesLabel = new JLabel();
            balancesLabel.setMinimumSize(new Dimension(20, 20));
            if (!checkBalance) {
                balancesLabel.setIcon(new ImageIcon(OfferingPanel.class.getResource("/icons/check.png")));
                balances = true;
            }

            updateBalances();
        }

        public void updateTotal()
        {
            if (checkBalance) {
                updateBalances();
                clearInputs();
            }
        }

        private void clearInputs()
        {
            currencyField.setText("");
            checksField.setText("");
        }

        private void updateBalances()
        {
            final String currencyText = currencyField.getText();
            final Double currency = currencyText.isEmpty() ? 0.0 : Double.parseDouble(currencyText);
            final String checksText = checksField.getText();
            final Double checks = checksText.isEmpty() ? 0.0 : Double.parseDouble(checksText);
            final Double total = currency + checks;
            
            if (checkBalance)
            {
                final Double totalForCategory = getTotalForCategory(categoryName);
                totalField.setText(simpleCurrencyFormat.format(totalForCategory));
                if (total.equals(totalForCategory)) {
                    balancesLabel.setIcon(new ImageIcon(OfferingPanel.class.getResource("/icons/check.png")));
                    balances = true;
                } else {
                    balancesLabel.setIcon(null);
                    balances = false;
                }
            } else {
                totalField.setText(simpleCurrencyFormat.format(total));
            }

            updateOfferingBalances();
            invalidate();
            updateUI();
        }

        public JLabel getCategoryNameLabel()
        {
            return categoryNameLabel;
        }

        public JFormattedTextField getCurrencyField()
        {
            return currencyField;
        }

        public JFormattedTextField getChecksField()
        {
            return checksField;
        }

        public JFormattedTextField getTotalField()
        {
            return totalField;
        }

        public JLabel getBalancesLabel()
        {
            return balancesLabel;
        }

        public boolean isBalanced()
        {
            return balances;
        }
        
        public Double getCurrency()
        {
            try {
                return simpleCurrencyFormat.parse(currencyField.getText()).doubleValue();
            } catch (ParseException e) {
            }
            return 0.0;
        }
        
        public Double getChecks()
        {
            try {
                return simpleCurrencyFormat.parse(checksField.getText()).doubleValue();
            } catch (ParseException e) {
            }
            return 0.0;
        }

        public Double getTotal()
        {
            try {
                return simpleCurrencyFormat.parse(totalField.getText()).doubleValue();
            } catch (ParseException e) {
            }
            return 0.0;
        }
    }

    private void updateOfferingBalances()
    {
        totalForAllCategories = 0.0;
        boolean categoriesBalanced = true;
        for (CategoryInputFields category : categoryInputs.values()) {
            totalForAllCategories += category.getTotal();
            if (!category.isBalanced()) {
                categoriesBalanced = false;
            }
        }

        offeringTotalField.setText(simpleCurrencyFormat.format(totalForAllCategories));

        if (categoriesBalanced) {
            offeringBalancesLabel.setIcon(new ImageIcon(OfferingPanel.class.getResource("/icons/check.png")));
        } else {
            offeringBalancesLabel.setIcon(null);
        }

        invalidate();
        updateUI();
    }

    private Double getTotalForCategory(String category)
    {
        Double total = 0.0;
        for (GivingRecord record : recordsForDate) {
            total += record.getAmountForCategory(category);
        }
        return total;
    }

    public JRDataSource createDataSource() 
    {  
        final DRDataSource dataSource = new DRDataSource("category", "currency", "checks", "total");  
        for (String category : categoryInputs.keySet()) {
            final CategoryInputFields input = categoryInputs.get(category);
            final BigDecimal currency = new BigDecimal(input.getCurrency());
            final BigDecimal checks = new BigDecimal(input.getChecks());
            final BigDecimal total = new BigDecimal(input.getTotal());
            dataSource.add(category, currency, checks, total);
        }
        return dataSource;  
    }

    public BigDecimal getOfferingTotal()
    {
        return new BigDecimal(totalForAllCategories);
    }
}
