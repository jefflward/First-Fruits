package com.wardware.givingtracker.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
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
        
        final JTableHeader header = getTableHeader();
        header.addMouseListener( new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                final JTableHeader h = (JTableHeader)e.getSource();
                final int nColumn = h.columnAtPoint(e.getPoint());
                if (nColumn != -1) {
                    model.sortByColumn(nColumn);
                }
            }
        });
        header.setReorderingAllowed(false);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
                    // Selects the row under the click when there is no 
                    // selection or only a single row selected
                    final int[] selectedRows = getSelectedRows();
                    final String deleteActionText = selectedRows.length > 1 ? "Delete selected rows" : "Delete selected row";
                    
                    if (selectedRows.length <= 1) {
                        final int r = rowAtPoint(e.getPoint());
                        if (r >= 0 && r < getRowCount()) {
                            setRowSelectionInterval(r, r);
                        }
                    }

                    final JPopupMenu popup = new JPopupMenu();
                    popup.add(new TextAction(deleteActionText) {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            deleteSelectedRecords();
                        }
                    });
                    popup.add(new TextAction("Clear selection") {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            clearSelection();
                        }
                    });
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent event) {
                if (KeyEvent.VK_DELETE == event.getKeyCode()) {
                    deleteSelectedRecords();
                }
            }
        });
    }
    
    @Override  
    public void changeSelection(int rowIndex, int columnIndex,  
            boolean toggle, boolean extend) {  
        super.changeSelection(rowIndex, columnIndex, true, false);  
    } 
    
    public boolean deleteSelectedRecords()
    {
        final int choice = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete the selected records?", "Delete selected records", 
                        JOptionPane.YES_NO_CANCEL_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            final int[] selectedRows = getSelectedRows();
            if (selectedRows.length > 0) {
                final List<GivingRecord> records = model.getRecords(selectedRows);
                RecordManager.getInstance().deleteRecords(records);
                return true;
            }
        }
        return false;
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
