package com.wardware.givingtracker.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class TallyTable extends JTable
{
    private TallyTableModel model;

    public TallyTable()
    {
        model = new TallyTableModel();
        setModel(model);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        final TableColumnModel m = getColumnModel();
        final TableColumn column = m.getColumn(0);
        column.setCellRenderer(NumberRenderer.getCurrencyRenderer());
        
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent event) {
                if (KeyEvent.VK_DELETE == event.getKeyCode()) {
                    deleteSelection();
                }
            }
        });
    }
    
    private void deleteSelection()
    {
        final int selection = getSelectedRow();
        if (selection != -1) {
            model.removeRow(selection);
            if (model.getRowCount() > selection) {
                getSelectionModel().setSelectionInterval(selection, selection);
            } 
        }
    }

    public TallyTableModel getModel()
    {
        return model;
    }
    
    public void addValue(BigDecimal value)
    {
        model.addValue(value);
    }
}
