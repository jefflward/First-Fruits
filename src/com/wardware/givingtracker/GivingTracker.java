package com.wardware.givingtracker;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.wardware.givingtracker.ui.GivingTrackerFrame;

public class GivingTracker
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final GivingTrackerFrame givingTrackerFrame = new GivingTrackerFrame();
                givingTrackerFrame.setVisible(true);
            }
        });
    }
}
