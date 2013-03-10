package us.wardware.firstfruits.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.DefaultTableModel;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.Settings;


public class RecordTableModel extends DefaultTableModel implements Observer
{
    private List<GivingRecord> records;

    private ArrayList<String> columnNames;

    public RecordTableModel()
    {
        records = new ArrayList<GivingRecord>();
        columnNames = new ArrayList<String>();
        columnNames.add("Date");
        columnNames.add("Last Name");
        columnNames.add("First Name");
        columnNames.addAll(Settings.getInstance().getCategories());
        columnNames.add("Total");
        this.setColumnIdentifiers(columnNames.toArray());
        Settings.getInstance().addObserver(this);
    }
    
    private void updateColumns()
    {
        columnNames = new ArrayList<String>();
        columnNames.add("Date");
        columnNames.add("Name");
        columnNames.addAll(Settings.getInstance().getCategories());
        columnNames.add("Total");
        this.setColumnIdentifiers(columnNames.toArray());
        
        fireTableStructureChanged();
    }
    
    public String[] getColumns()
    {
        final String[] columns = new String[getColumnCount()];
        columnNames.toArray(columns);
        return columns;
    }
    
    @Override
    public String getColumnName(int column)
    {
        return getColumns()[column];
    }
    
    public void setRecords(List<GivingRecord> records)
    {
        Collections.sort(records);
        this.records = records;
        fireTableDataChanged();
    }
    
    public void addGivingRecord(GivingRecord record)
    {
        records.add(record);
        fireTableDataChanged();
    }
    
    @Override
    public int getRowCount()
    {
        return records == null ? 0 : records.size();
    }
   
    @Override
    public Object getValueAt(int row, int column)
    {
        final GivingRecord record = records.get(row);
        if (record != null) {
            if (column == 0) {
                return record.getDate();
            } else if (column == 1) {
                return record.getLastName();
            } else if (column == 2) {
                return record.getFirstName();
            } else if (column == getColumnCount() - 1) {
                return record.getTotal();
            } else {
                final String columnName = getColumnName(column);
                return record.getAmountForCategory(columnName);
            }
        }
        return null;
    }
    
    @Override
    public Class<?> getColumnClass(int column) 
    {
        switch (column) {
            case 0:
                return Date.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return Double.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
    
    public GivingRecord getRecord(int row)
    {
        if (row >= 0 && row < records.size()) {
            return records.get(row);
        }
        
        return null;
    }
    
    public List<GivingRecord> getRecords(int[] rows)
    {
        final List<GivingRecord> records = new ArrayList<GivingRecord>();
        for (int row : rows) {
            final GivingRecord record = getRecord(row);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        updateColumns();
    }
}
