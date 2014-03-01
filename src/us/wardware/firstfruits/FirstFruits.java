package us.wardware.firstfruits;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import us.wardware.firstfruits.ui.FirstFruitsFrame;
import us.wardware.firstfruits.ui.SettingsDialog;

public class FirstFruits
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
                final FirstFruitsFrame firstFruitsFrame = new FirstFruitsFrame();
                firstFruitsFrame.setVisible(true);
                
                if (Settings.getInstance().getInstallDate() == null) {
                    Settings.getInstance().setInstallDate(SimpleDateFormat.getDateTimeInstance().format(new Date()));
                    final SettingsDialog initialSettings = new SettingsDialog(firstFruitsFrame, true);
                    initialSettings.setVisible(true);
                    initialSettings.toFront();
                    initialSettings.repaint();
                }
            }
        });
    }
}
