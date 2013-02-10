package com.wardware.givingtracker.ui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class CurrencyFormattedTextField extends JFormattedTextField
{
    private static final NumberFormat simpleCurrencyFormat;
    
    static {
        simpleCurrencyFormat = NumberFormat.getNumberInstance();
        simpleCurrencyFormat.setMaximumFractionDigits(2);
        simpleCurrencyFormat.setMinimumFractionDigits(2);
    }
    
    public CurrencyFormattedTextField()
    {
        super(simpleCurrencyFormat);
        addFocusListener(new SelectAllFocusListener());
        setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    @Override  
    protected void processFocusEvent(final FocusEvent e) {  
        if (e.isTemporary()) {  
            return;  
        }  

        if (e.getID() == FocusEvent.FOCUS_LOST) {  
            if (getText() == null || getText().isEmpty()) {  
                setValue(null);  
            }  
        }  
        super.processFocusEvent(e);  
    } 
    
    private class SelectAllFocusListener extends FocusAdapter
    {
        @Override
        public void focusGained(final FocusEvent event)
        {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final Component component = event.getComponent();
                    if (component instanceof JFormattedTextField) {
                        ((JFormattedTextField) component).selectAll();
                    }
                }
            });
        }
    }
}
