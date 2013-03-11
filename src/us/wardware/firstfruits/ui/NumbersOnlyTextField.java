package us.wardware.firstfruits.ui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;

public class NumbersOnlyTextField extends JTextField
{
    public NumbersOnlyTextField()
    {
        ((AbstractDocument) getDocument()).setDocumentFilter(new NumbersOnlyFilter());
        
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
    }
}
