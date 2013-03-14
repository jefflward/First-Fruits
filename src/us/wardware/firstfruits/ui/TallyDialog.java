package us.wardware.firstfruits.ui;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TallyDialog extends JDialog
{
    private JLabel totalLabel;
    private TallyTable tallyTable;
    private CurrencyFormattedTextField valueField;
    
    public TallyDialog(JFrame owner)
    {
        super(owner);
        initComponents();
    }

    private void initComponents()
    {
        setLayout(new BorderLayout());
        final List<Image> icons = new ArrayList<Image>();
        icons.add(new ImageIcon(TallyDialog.class.getResource("/icons/tally_small.png")).getImage());
        setIconImages(icons);
        setModalityType(ModalityType.MODELESS);
        setPreferredSize(new Dimension(150, 274));

        setTitle("Quick Tally");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        totalLabel = new JLabel("Total: $0.00");
        //totalLabel.setHorizontalAlignment(JLabel.CENTER);
        add(totalLabel, BorderLayout.SOUTH);
        
        final JPanel middle = new JPanel(new BorderLayout());
        
        tallyTable = new TallyTable();
        tallyTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent event) {
                updateTotal();
            }
        });
        tallyTable.setEnabled(false);
        
        final JScrollPane scrollPane = new JScrollPane(tallyTable);
        scrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        middle.add(scrollPane, BorderLayout.CENTER);
        
        valueField = new CurrencyFormattedTextField(true); 
        valueField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent event) {
                if (KeyEvent.VK_ENTER == event.getKeyCode() || 
                    KeyEvent.VK_ADD == event.getKeyCode()) {
                    final String value = valueField.getText();
                    try {
                        final double doubleValue = NumberFormat.getNumberInstance().parse(value).doubleValue();
                        if (doubleValue != 0.0) {
                            tallyTable.addValue(new BigDecimal(doubleValue));
                        }
                    } catch (ParseException e) {
                    }
                    valueField.setText("");
                } else if (KeyEvent.VK_SUBTRACT == event.getKeyCode()) {
                    final String value = valueField.getText();
                    try {
                        final double doubleValue = NumberFormat.getNumberInstance().parse(value).doubleValue();
                        if (doubleValue != 0.0) {
                            tallyTable.addValue(new BigDecimal(doubleValue));
                            valueField.setText("-");
                        }
                    } catch (ParseException e) {
                    }
                } else if (KeyEvent.VK_ESCAPE == event.getKeyCode()) {
                    clearData();
                }
            }

        });
        valueField.requestFocusInWindow();
        valueField.setText("0.00");
        valueField.setSelectionStart(0);
        valueField.setSelectionEnd(valueField.getText().length()-1);
        
        middle.add(valueField, BorderLayout.SOUTH);
        
        add(middle, BorderLayout.CENTER);
        invalidate();
        pack();
    }
    
    private void clearData()
    {
        tallyTable.getModel().clearTableData();
        valueField.setText("");
    }
    
    private void updateTotal()
    {
        final double total = tallyTable.getModel().getTotal().doubleValue();
        totalLabel.setText("Total: " + NumberFormat.getCurrencyInstance().format(total));
    }
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                final TallyDialog dialog = new TallyDialog(null);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }
}
