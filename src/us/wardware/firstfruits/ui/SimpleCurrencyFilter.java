package us.wardware.firstfruits.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class SimpleCurrencyFilter extends DocumentFilter
{
    private boolean allowNegative;
    private final Pattern positiveCurrencyPattern;
    private final Pattern negativeCurrencyPattern;
    
    private final String startNegativeDecimal = "^-\\.$";
    
    public SimpleCurrencyFilter(boolean allowNegative)
    {
        this.allowNegative = allowNegative;
        
        final String positiveCurrencyRegex = "^\\$?([1-9]{1}[0-9]{0,2}(\\,[0-9]{3})*(\\.[0-9]{0,2})?|" +
        		"[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|" +
        		"0{0,1}(\\.[0-9]{0,2})?|" +
        		"(\\.[0-9]{1,2})?)$";
        
        final String negativeCurrencyRegex = "^(-{0,1}(?!0)\\$?[1-9]{1}[0-9]{0,2}(\\,[0-9]{3})*(\\.[0-9]{0,2})?|" +
        		"-{0,1}(?!0)[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|" +
        		"-{0,1}(?!0)(\\.[0-9]{1,2})?|" +
        		"-{0,1}(?!0)[0-9]{0,})$";
        
        
        positiveCurrencyPattern = Pattern.compile(positiveCurrencyRegex);
        negativeCurrencyPattern = Pattern.compile(negativeCurrencyRegex);
        
    }

    public void insertString(DocumentFilter.FilterBypass fb, int offset,
                    String text, AttributeSet attr) throws BadLocationException
    {
        final String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        final StringBuilder sb = new StringBuilder(currentText);
        sb.insert(offset, text);
        {
            final Matcher positiveMatcher = positiveCurrencyPattern.matcher(sb.toString());
            if (positiveMatcher.matches()) {
                fb.insertString(offset, text, attr);
            } else if (allowNegative) {
                final Matcher negativeMatcher = negativeCurrencyPattern.matcher(sb.toString());
                if (negativeMatcher.matches() || startNegativeDecimal.equals(sb.toString())) {
                    fb.insertString(offset, text, attr);
                }
            }
        }
    }

    // no need to override remove(): inherited version allows all removals

    public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
                    String text, AttributeSet attr) throws BadLocationException
    {
        final String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
        final StringBuilder sb = new StringBuilder(currentText);
        sb.replace(offset, offset + length, text);
        final Matcher positiveMatcher = positiveCurrencyPattern.matcher(sb.toString());
        if (positiveMatcher.matches()) {
            fb.replace(offset, length, text, attr);
        } else if (allowNegative) {
            final Matcher negativeMatcher = negativeCurrencyPattern.matcher(sb.toString());
            if (negativeMatcher.matches() || sb.toString().matches(startNegativeDecimal)) {
                fb.replace(offset, length, text, attr);
            }
        }
    }

    public static void main(String[] args)
    {
        final DocumentFilter dfilter = new SimpleCurrencyFilter(true);

        final JTextField jtf = new JTextField();
        ((AbstractDocument) jtf.getDocument()).setDocumentFilter(dfilter);

        final JFrame frame = new JFrame("NonNumericFilter");
        frame.getContentPane().add(jtf, java.awt.BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(240, 120);
        frame.setVisible(true);
    }
}
