package us.wardware.firstfruits.tools;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.codec.digest.DigestUtils;

public class SoftwareKeyGenerator
{
    private static String salt = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJPqhNw2TSclLmHiCLyI9Lc6CYrdUfLWIBXTqv1Al5qA0eoqm6FlulJOoNMApW5a7DjTkFMAdSPggCurr43Or1bTFUnxhSG6LN6IXfRDzL3pFfqcEOnvlhx4xRcyZtJR3tEPSK4Ycw8UUv45uxsD3Lo22lBKtlP6HmJeYp2hJZUXAgMBAAECgYEAj17aZqRPhijY21F/DFdnc43SoAHDo6/+q4leDFkmWUCRtvqTnQ4AWjUSV1MiBmQk4TPXiUxBPoQlV67y223BrtXjE5y79rg3Vp917NPIRzvgAshLi9QSQt+EF3fCPwWq8s4E41jbzN2LwNDErPxBY/g1ZaOaWVIL1HgwDGbVD3ECQQDjRlkZvD98Hk2tbtbciyxTQdhjUafTudvMOYNS6KVKl9jNju81jCC13r0V1DDgwT5FhsGg7mPoAo9buxziBihZAkEAppx1YzWlAVorg0AVztLX554EBPOl8UM0RDQlY+J/GK/l3SEItJ3Tz0Ox8WEmj0vT2wK3IgFOPz+e+Xa3ihf67wJBAL/MX2criXvqau1BC8xFGxwMBlwSzgoM4GPh6WSvdsaTjOA0jsTmw7gYCASC9NjH3BB6n697Xw6uEmN8beyE+8kCQAtFEAeq7ENgJwtUvWTlDCeln536ISJlqlZmtJTCEFjKJSFFB8K33kjpylKKgMI2ndj1oQ+SyQksM4OqTRXxM08CQFyiMujiKm73NR41CI3AxTYbL0XKFLe6sy7izkuJaGm2gIDi/Rer/E7wuRcGd7DZPpMnD7jPnlzV5ROmuXajCBU=";
   
    public static String generateKey(String churchName, String version)
    {
        final Pattern versionPattern = Pattern.compile("(\\d+)\\.\\d+\\.\\d+");
        final Matcher m = versionPattern.matcher(version);
        if (m.matches()) {
            final String majorVersion = m.group(1);
            return DigestUtils.sha1Hex(churchName.trim().toUpperCase().replaceAll(" ", "") + majorVersion + salt);
        }
        return DigestUtils.sha1Hex(churchName.trim().toUpperCase().replaceAll(" ", "") + version + salt);
    }
    
    /**
     * @param args
     * @throws IOException 
     * @throws GeneralSecurityException 
     */
    public static void main(String[] args) throws GeneralSecurityException, IOException
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                String input = JOptionPane.showInputDialog("Church Name: "); 
                if (input == null || input.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Church name can not be empty", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    final Properties p = new Properties();
                    p.load(SoftwareKeyGenerator.class.getResourceAsStream("/version.properties"));
                    final String versionProperty = p.getProperty("version");
                    JOptionPane.showInputDialog("Key: ", generateKey(input, versionProperty));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Failed to load version properties", "Could not generate key", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });
    }
}
