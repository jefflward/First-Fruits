package us.wardware.firstfruits.ui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SelectAllFocusListener extends FocusAdapter
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