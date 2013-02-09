package com.wardware.givingtracker.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.TextAction;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.RecordManager;

public class RecordsTable extends JTable implements Observer
{
    private RecordTableModel model;

    public RecordsTable()
    {
        model = new RecordTableModel();
        setModel(model);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        RecordManager.getInstance().addObserver(this);
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                final int selectedRowCount = getSelectedRowCount();
                RecordManager.getInstance().setSelectionCount(selectedRowCount);
                if (selectedRowCount == 1) {
                    final int row = getSelectedRow();
                    RecordManager.getInstance().setSelectedRecord(model.getRecord(row));
                } else {
                    RecordManager.getInstance().setSelectedRecord(null);
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    // Selects the row under the click when there is no 
                    // selection or only a single row selected
                    final int[] selectedRows = getSelectedRows();
                    if (selectedRows.length <= 1) {
                        final int r = rowAtPoint(e.getPoint());
                        if (r >= 0 && r < getRowCount()) {
                            setRowSelectionInterval(r, r);
                        }
                    }

                    final JPopupMenu popup = new JPopupMenu();
                    popup.add(new TextAction("Delete Selected Row(s)") {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            deleteSelectedRecords();
                        }
                    });
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }
    
    @Override  
    public void changeSelection(int rowIndex, int columnIndex,  
            boolean toggle, boolean extend) {  
        super.changeSelection(rowIndex, columnIndex, true, false);  
    } 
    
    public void deleteSelectedRecords()
    {
        final int[] selectedRows = getSelectedRows();
        if (selectedRows.length > 0) {
            final List<GivingRecord> records = model.getRecords(selectedRows);
            RecordManager.getInstance().deleteRecords(records);
        }
    }
    
    public void addGivingRecord(GivingRecord record)
    {
        model.addGivingRecord(record);
    }

    @Override
    public void update(Observable o, Object value)
    {
        if (value instanceof GivingRecord) {
            model.fireTableRowsUpdated(getSelectedRow(), getSelectedRow());
            clearSelection();
        } else if (value instanceof List) {
            model.setRecords(RecordManager.getInstance().getRecords());
        } else if (value instanceof Boolean) {
            model.setRecords(RecordManager.getInstance().getRecords());
        }
    }
}
