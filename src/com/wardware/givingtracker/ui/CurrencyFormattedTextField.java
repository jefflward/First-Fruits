package com.wardware.givingtracker.ui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

import org.apache.commons.lang3.StringUtils;

public class CurrencyFormattedTextField extends JTextField
{
    public CurrencyFormattedTextField()
    {
        ((AbstractDocument) getDocument()).setDocumentFilter(new SimpleCurrencyFilter());
        
        addFocusListener(new SelectAllFocusListener());
        setHorizontalAlignment(SwingConstants.RIGHT);
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
                    if (component instanceof JTextField) {
                        ((JTextField) component).selectAll();
                    }
                }
            });
        }
        
        @Override
        public void focusLost(FocusEvent arg0)
        {
            padTwoDecimalPlacesIfNeeded();
            firePropertyChange("focusLost", false, true);
            super.focusLost(arg0);
        }
    }
    
    private void padTwoDecimalPlacesIfNeeded()
    {
        final String currentText = getText();
        if (currentText.contains(".")) {
            int digitsAfterDecimal = (currentText.length() - 1) - currentText.indexOf(".");
            int padLength = currentText.length() + (2 - digitsAfterDecimal);
            final String paddedValue = StringUtils.rightPad(currentText, padLength, "0");
            setText(paddedValue);
        } else if (!currentText.isEmpty()){
            setText(currentText + ".00");
        }
    }
}
