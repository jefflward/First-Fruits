package us.wardware.firstfruits;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import us.wardware.firstfruits.tools.SoftwareKeyGenerator;
import us.wardware.firstfruits.ui.FirstFruitsFrame;
import us.wardware.firstfruits.ui.RegistrationDialog;
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
                
                final String registrationName = Settings.getInstance().getRegistrationName();
                final String registrationKey = Settings.getInstance().getRegistrationKey();
                if (registrationKey.isEmpty()){
                    final RegistrationDialog rd = new RegistrationDialog(firstFruitsFrame);
                    rd.setVisible(true);
                    rd.toFront();
                } else {
                    final Properties p = new Properties();
                    try {
                        p.load(FirstFruits.class.getResourceAsStream("/version.properties"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final String versionProperty = p.getProperty("version");
                    final String keyForVersion = SoftwareKeyGenerator.generateKey(registrationName, versionProperty);
                    
                    if (!registrationKey.equals(keyForVersion)) {
                        JOptionPane.showMessageDialog(firstFruitsFrame, 
                                        "<HTML><B>First Fruits</B>" +
                                        "<BR>The registration key is not valid for this version of the software. The program will now exit.", 
                                        "Registration Version Error", 
                                        JOptionPane.ERROR_MESSAGE,
                                        new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo48.png")));
                        System.exit(1);
                    }
                }
                
                if (Settings.getInstance().getInstallDate() == null) {
                    firstFruitsFrame.setVisible(true);
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
