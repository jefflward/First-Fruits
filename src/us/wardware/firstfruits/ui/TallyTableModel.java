package us.wardware.firstfruits.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class TallyTableModel extends DefaultTableModel
{
    private BigDecimal total;
    private List<BigDecimal> values;
    
    public TallyTableModel()
    {
        total = BigDecimal.ZERO;
        values = new ArrayList<BigDecimal>();
    }

    public BigDecimal getTotal()
    {
        return total;
    }
    
    public void addValue(BigDecimal value)
    {
        values.add(value);
        updateTotal();
        fireTableDataChanged();
    }
    
    @Override
    public void removeRow(int row)
    {
        if (row < values.size()) {
            values.remove(row);
            updateTotal();
            fireTableDataChanged();
        }
    }
    
    private void updateTotal()
    {
        total = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            total = total.add(value);
        }
    }

    @Override
    public String getColumnName(int column)
    {
        return null;
    }
    
    @Override
    public int getRowCount()
    {
        return values == null ? 0 : values.size();
    }
    
    @Override
    public int getColumnCount()
    {
        return 1;
    }
   
    @Override
    public Object getValueAt(int row, int column)
    {
        if (row < values.size()) {
            return values.get(row).doubleValue();
        }
        return new Double(0.0);
    }
    
    @Override
    public Class<?> getColumnClass(int column) 
    {
        return Double.class;
    }
    
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }
}