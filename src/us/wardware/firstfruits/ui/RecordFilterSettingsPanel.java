package us.wardware.firstfruits.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.wardware.firstfruits.RecordFilter;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;
import us.wardware.firstfruits.util.DateUtils;

import com.michaelbaranov.microba.calendar.DatePicker;

public class RecordFilterSettingsPanel extends JPanel implements Observer
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private JComboBox filterByCombo;
    private JComboBox operationCombo;
    private JTextField valueField;
    private DatePicker datePicker;
    private RecordFilter recordFilter;
    private Component valueComponent;

    public RecordFilterSettingsPanel()
    {
        recordFilter = RecordManager.getInstance().getRecordFilter();
        initComponents();
        Settings.getInstance().addObserver(this);
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        add(new JLabel("Filter by: "), Gbc.xyi(0,0,2));
       
        final List<String> filterTypes = new ArrayList<String>();
        filterTypes.add("");
        filterTypes.add("Date");
        filterTypes.add("Last Name");
        filterTypes.add("First Name");
        filterTypes.addAll(Settings.getInstance().getCategories());
        filterTypes.add("Total"); 
        filterByCombo = new JComboBox(filterTypes.toArray());
        filterByCombo.setMinimumSize(new Dimension(150, filterByCombo.getHeight()));
        filterByCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (filterByCombo.getSelectedItem() != null) {
                    final String selection = filterByCombo.getSelectedItem().toString();
                    recordFilter.setType(selection);
                    if (selection.equals("Date")) {
                        if (valueComponent != datePicker) {
                            remove(valueComponent);
                            valueComponent = datePicker;
                            add(valueComponent, Gbc.xyi(3,0,2).west().horizontal());
                            if (datePicker.getDate() != null) {
                                recordFilter.setDateValue(datePicker.getDate());
                            }
                            invalidate();
                            updateUI();
                        }
                    } else if (valueComponent == datePicker) {
                        remove(valueComponent);
                        valueComponent = valueField;
                        add(valueComponent, Gbc.xyi(3,0,2).west().horizontal());
                    } else {
                        valueField.setText("");
                        recordFilter.setValue("");
                    }
                    RecordManager.getInstance().setRecordFilter(recordFilter);
                }
            }
        });
        add(filterByCombo, Gbc.xyi(1,0,2));
        
        operationCombo = new JComboBox(new Object[]{"=",">","<"});
        operationCombo.setMinimumSize(new Dimension(150, operationCombo.getHeight()));
        operationCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (operationCombo.getSelectedItem() != null) {
                    final String selection = operationCombo.getSelectedItem().toString();
                    recordFilter.setOperation(selection);
                    RecordManager.getInstance().setRecordFilter(recordFilter);
                }
            }
        });
        add(operationCombo, Gbc.xyi(2,0,2));
        
        valueField = new JTextField(20);
        valueField.setMinimumSize(valueField.getPreferredSize());
        valueField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                recordFilter.setValue(valueField.getText());
                RecordManager.getInstance().setRecordFilter(recordFilter);
            }
        });
        valueComponent = valueField;
        add(valueComponent, Gbc.xyi(3,0,2).west().horizontal());
        
        datePicker = new DatePicker(DateUtils.trim(new Date()));
        datePicker.setPreferredSize(new Dimension(150, datePicker.getPreferredSize().height));
        datePicker.setDateFormat(SDF);
        datePicker.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                if (datePicker.getDate() != null) {
                    recordFilter.setDateValue(datePicker.getDate());
                    RecordManager.getInstance().setRecordFilter(recordFilter);
                }
            }
        });
    }

    @Override
    public void update(Observable arg0, Object arg1)
    {
        updateFilterTypes();
    }

    private void updateFilterTypes()
    {
        final List<String> filterTypes = new ArrayList<String>();
        filterTypes.add("");
        filterTypes.add("Date");
        filterTypes.add("Name");
        filterTypes.addAll(Settings.getInstance().getCategories());
        filterTypes.add("Total");
        
        filterByCombo.removeAllItems();
        for (String type : filterTypes) {
            filterByCombo.addItem(type);
        }
    }
}
