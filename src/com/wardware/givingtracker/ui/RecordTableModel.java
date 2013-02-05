package com.wardware.givingtracker.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import com.wardware.givingtracker.GivingRecord;

public class RecordTableModel extends DefaultTableModel
{
    private static final String[] COLUMNS = new String[]{"Date", "Name", "General", "Missions", "Building", "Total"};
    
    private List<GivingRecord> records;
    
    public RecordTableModel()
    {
        super(0, COLUMNS.length);
        records = new ArrayList<GivingRecord>();
    }
    
    public static String[] getColumns()
    {
        return COLUMNS;
    }
    
    @Override
    public String getColumnName(int column)
    {
        return COLUMNS[column];
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
            switch (column) {
            case 0: return record.getDate();
            case 1: return record.getName();
            case 2: return NumberFormat.getCurrencyInstance().format(record.getGeneral());
            case 3: return NumberFormat.getCurrencyInstance().format(record.getMissions());
            case 4: return NumberFormat.getCurrencyInstance().format(record.getBuilding());
            case 5: return NumberFormat.getCurrencyInstance().format(record.getTotal());
            default:
                return null;
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
}
