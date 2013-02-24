package com.wardware.givingtracker.ui;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.DefaultTableModel;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.Settings;

public class RecordTableModel extends DefaultTableModel implements Observer
{
    private List<GivingRecord> records;

    private ArrayList<String> columnNames;

    private String sortColumn;

    private boolean reverseSort;
    
    public RecordTableModel()
    {
        sortColumn = "";
        reverseSort = false;
        records = new ArrayList<GivingRecord>();
        columnNames = new ArrayList<String>();
        columnNames.add("Date");
        columnNames.add("Name");
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
                return record.getName();
            } else if (column == getColumnCount() - 1) {
                return NumberFormat.getCurrencyInstance().format(record.getTotal());
            } else {
                final String columnName = getColumnName(column);
                // MIght not have a category value
                Double amount = record.getAmountForCategory(columnName);
                if (amount != null)
                {
                    return NumberFormat.getCurrencyInstance().format(record.getAmountForCategory(columnName));
                }
            }
        }
        return null;
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

    public void sortByColumn(int nColumn)
    {
        final String columnName = getColumnName(nColumn);
        if (columnName.equals(sortColumn)) {
            reverseSort = !reverseSort;
        } else {
            reverseSort = false;
        }
        sortColumn = columnName;
        Collections.sort(records, new Comparator<GivingRecord>(){
            @Override
            public int compare(GivingRecord lhs, GivingRecord rhs) {
                if (sortColumn.equals("Date")) {
                    final DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    try {
                        final Date lhsDate = sdf.parse(lhs.getDate());
                        final Date rhsDate = sdf.parse(rhs.getDate());
                        if (reverseSort) {
                            return rhsDate.compareTo(lhsDate);
                        }
                        return lhsDate.compareTo(rhsDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (sortColumn.equals("Name")) {
                    if (reverseSort) {
                        return rhs.getName().compareTo(lhs.getName());
                    }
                    return lhs.getName().compareTo(rhs.getName());
                } else if (sortColumn.equals("Total")) {
                    if (reverseSort) {
                        return rhs.getTotal().compareTo(lhs.getTotal());
                    }
                    return lhs.getTotal().compareTo(rhs.getTotal());
                } else if (Settings.getInstance().getCategories().contains(sortColumn)) {
                    if (reverseSort) {
                        return rhs.getAmountForCategory(sortColumn).compareTo(lhs.getAmountForCategory(sortColumn));
                    }
                    return lhs.getAmountForCategory(sortColumn).compareTo(rhs.getAmountForCategory(sortColumn));
                }
                return lhs.compareTo(rhs);
            }
        });
        fireTableDataChanged();
    }
}
