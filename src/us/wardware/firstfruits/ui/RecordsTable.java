package us.wardware.firstfruits.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultRowSorter;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.TextAction;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordFilter;
import us.wardware.firstfruits.RecordManager;

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
                    int selection = convertRowIndexToModel(row);
                    RecordManager.getInstance().setSelectedRecord(model.getRecord(selection));
                } else {
                    RecordManager.getInstance().setSelectedRecord(null);
                }
            }
        });
        
        final JTableHeader header = getTableHeader();
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
        
        final TableRowSorter<RecordTableModel> sorter = new TableRowSorter<RecordTableModel>(model);
        sorter.setRowFilter(new RecordTableRowFilter());
        setRowSorter(sorter);
        
        setDefaultRenderer(Date.class, FormatRenderer.getSimpleDateRenderer());
        setDefaultRenderer(Double.class, NumberRenderer.getCurrencyRenderer());
    }
    
    private class RecordTableRowFilter extends RowFilter<RecordTableModel, Integer>
    {
        @Override
        public boolean include(javax.swing.RowFilter.Entry<? extends RecordTableModel, ? extends Integer> entry)
        {
            final RecordTableModel recordTableModel = entry.getModel();
            final GivingRecord record = recordTableModel.getRecord(entry.getIdentifier());
            final RecordFilter recordFilter = RecordManager.getInstance().getRecordFilter();
            if (recordFilter == null || !recordFilter.isEnabled() || recordFilter.isMatch(record)) {
                return true;
            }
            return false;
        }
    }
    
    @Override  
    public void changeSelection(int rowIndex, int columnIndex,  
            boolean toggle, boolean extend) {  
        super.changeSelection(rowIndex, columnIndex, true, false);  
    } 
    
    public boolean deleteSelectedRecords()
    {
        final int[] selectedRows = getSelectedRows();
        final String plural = selectedRows.length > 1 ? "s" : "";
        if (selectedRows.length > 0) {
            final int choice = JOptionPane.showConfirmDialog(this, 
                            "Are you sure you want to delete the selected record" +  plural + "?", 
                            "Delete selected record" + plural, 
                            JOptionPane.YES_NO_CANCEL_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
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
            int selection = convertRowIndexToModel(getSelectedRow());
            model.fireTableRowsUpdated(selection, selection);
            clearSelection();
        } else if (value instanceof List) {
            model.setRecords(RecordManager.getInstance().getRecords());
            ((DefaultRowSorter<?,?>)getRowSorter()).sort();
        } else if (value instanceof RecordFilter) {
            model.setRecords(RecordManager.getInstance().getRecords());
            ((DefaultRowSorter<?,?>)getRowSorter()).sort();
        }
    }
}
