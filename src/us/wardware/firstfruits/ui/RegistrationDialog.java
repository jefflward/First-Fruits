package us.wardware.firstfruits.ui;

import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.TextAction;

import us.wardware.firstfruits.Settings;
import us.wardware.firstfruits.tools.SoftwareKeyGenerator;

public class RegistrationDialog extends JDialog
{
    private boolean trialEnded;
    protected boolean registerSuccessful;

    public RegistrationDialog(JFrame owner)
    {
        super(owner);
        addWindowListener(new WindowClosingListener());
        initComponents();
        setLocationRelativeTo(owner);
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        setModalityType(ModalityType.APPLICATION_MODAL);
        setAlwaysOnTop(true);
        setTitle("First Fruits Registration");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        add(new JLabel(new ImageIcon(RegistrationDialog.class.getResource("/icons/logo48.png"))), Gbc.xyi(0,0,2));
        
        // for copying style
        JLabel label = new JLabel();
        Font font = label.getFont();

        // create some css from the label's font
        StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
        style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
        style.append("font-size:" + font.getSize() + "pt;");

        // html content
        JEditorPane ep = new JEditorPane("text/html", 
                "<HTML><BODY style=\"" + style + "\">" +
                "<B>Thank you for choosing First Fruits by WardWare.</B>" +
        		"<BR><BR>To unlock your trial version please enter your product registration key." +
                "<BR>To purchase a registration key visit <a href=\"http://www.wardware.us\">www.wardware.us</a>" +
        		"</BODY></HTML>");

        // handle link events
        ep.addHyperlinkListener(new HyperlinkListener()
        {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e)
            {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                    }
            }
        });
        ep.setEditable(false);
        ep.setBackground(label.getBackground());
        
        add(ep, Gbc.xyi(1,0,2).left(10).horizontal().gridWidth(2));
        
        final JPanel inputPanel = new JPanel(new GridBagLayout());
        final JLabel churchNameLabel = new JLabel("Church Name", JLabel.LEADING);
        inputPanel.add(churchNameLabel, Gbc.xyi(0,0,2).left(10).top(10).west());
        
        final JTextField churchName = new JTextField(15);
        inputPanel.add(churchName, Gbc.xyi(1,0,2).horizontal().top(10).right(5));
        
        final JLabel keyLabel = new JLabel("Registration Key", JLabel.LEADING);
        inputPanel.add(keyLabel, Gbc.xyi(0,1,2).left(10).top(5));
        
        final JTextField keyField = new JTextField(15);
        inputPanel.add(keyField, Gbc.xyi(1,1,2).horizontal().top(5).right(5));
        
        add(inputPanel, Gbc.xyi(1,1,2).gridWidth(2).gridHeight(2).horizontal());
        
        Date installDate = new Date();
        try {
            installDate = SimpleDateFormat.getDateTimeInstance().parse(Settings.getInstance().getInstallDate());
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        final Properties p = new Properties();
        try {
            p.load(RegistrationDialog.class.getResourceAsStream("/version.properties"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        final int trialPeriodDays = Integer.parseInt(p.getProperty("t", "30"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(installDate);
        cal.add(Calendar.DATE, trialPeriodDays);
        
        long days = (cal.getTimeInMillis() - now.getTimeInMillis()) / 86400000;
        if (days <= 0) {
            trialEnded = true;
            add(new JLabel(String.format("<HTML><B>Your trial period has ended. You must register to continue.</B></HTML>", days)), Gbc.xyi(1,3,2).left(10).top(10).horizontal());
        } else {
            add(new JLabel(String.format("<HTML>Your trial period has <B>%d</B> days remaining.</HTML>", days)), Gbc.xyi(1,3,2).left(10).top(10).horizontal());
        }
        
        final JPanel buttonPanel = new JPanel(new GridBagLayout());
        final JButton continueButton = new JButton(new TextAction("Continue with Trial"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
            }
        });
        if (days > 0) {
            buttonPanel.add(continueButton, Gbc.xyi(0,0,2));
        }
        
        final JButton registerButton = new JButton(new TextAction("Register Now"){
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final String keyFromInput = SoftwareKeyGenerator.generateKey(churchName.getText(), p.getProperty("version"));
                if (keyFromInput.equals(keyField.getText().trim())) {
                    Settings.getInstance().setRegistrationKey(keyFromInput);
                    Settings.getInstance().setRegistrationName(churchName.getText());
                    registerSuccessful = true;
                    setVisible(false);
                    JOptionPane.showMessageDialog(RegistrationDialog.this, 
                                    "<HTML><B>First Fruits</B>" +
                                    "<BR>Registration complete, thank you!", 
                                    "Registration Successful", 
                                    JOptionPane.INFORMATION_MESSAGE,
                                    new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo48.png")));
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(RegistrationDialog.this, 
                                    "<HTML><B>First Fruits</B>" +
                                    "<BR>The registration key you entered was not valid. Please try again.", 
                                    "Registration Error", 
                                    JOptionPane.ERROR_MESSAGE,
                                    new ImageIcon(FirstFruitsFrame.class.getResource("/icons/logo48.png")));
                }
            }
        });
        
        buttonPanel.add(registerButton, Gbc.xyi(1,0,2));
        add(buttonPanel, Gbc.xyi(1,5,2).gridWidth(2).top(10));
        
        pack();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final RegistrationDialog dialog = new RegistrationDialog(null);
                dialog.setVisible(true);
                dialog.setAlwaysOnTop(true);
            }
        });
    }
    
    private class WindowClosingListener extends WindowAdapter
    {
        @Override
        public void windowClosed(WindowEvent e)
        {
            if (trialEnded && !registerSuccessful) {
                System.exit(1);
            }
        }
    }
}
