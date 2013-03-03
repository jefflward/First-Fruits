package com.wardware.givingtracker.ui;

import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class AutoTextField extends JTextField
{
    private List<?> dataList;
    private boolean isCaseSensitive;
    private boolean isStrict;
    private AutoComboBox autoComboBox;
    
    class AutoDocument extends PlainDocument
    {
        public void replace(int i, int j, String s, AttributeSet attributeset) throws BadLocationException
        {
            // If string didn't change don't try and find match
            if (s != null && s.equals(getText(0, getLength()))) {
                return;
            }
            super.remove(i, j);
            insertString(i, s, attributeset);
        }

        public void insertString(int i, String s, AttributeSet attributeset) throws BadLocationException
        {
            if (s == null || s.isEmpty())
                return;
            final String text = getText(0, i);
            String match = getMatch(text + s);
            int j = (i + s.length()) - 1;
            if (isStrict && match == null) {
                match = getMatch(text);
                j--;
            } else if (!isStrict && match == null) {
                super.insertString(i, s, attributeset);
                return;
            }
            if (autoComboBox != null && match != null)
                autoComboBox.setSelectedValue(match);
            super.remove(0, getLength());
            super.insertString(0, match, attributeset);
            setSelectionStart(j + 1);
            setSelectionEnd(getLength());
        }

        public void remove(int i, int j) throws BadLocationException
        {
            super.remove(i, j);
        }
    }

    public AutoTextField(List<?> list)
    {
        isCaseSensitive = false;
        isStrict = true;
        autoComboBox = null;
        if (list == null) {
            throw new IllegalArgumentException("values can not be null");
        } else {
            dataList = list;
            init();
            return;
        }
    }

    AutoTextField(List<?> list, AutoComboBox b)
    {
        isCaseSensitive = false;
        isStrict = true;
        autoComboBox = null;
        if (list == null) {
            throw new IllegalArgumentException("values can not be null");
        } else {
            dataList = list;
            autoComboBox = b;
            init();
            return;
        }
    }

    private void init()
    {
        setDocument(new AutoDocument());
        if (isStrict && dataList.size() > 0)
            setText(dataList.get(0).toString());
    }

    private String getMatch(String input)
    {
        for (Object item : dataList) {
            final String itemString = item.toString();
            if (itemString != null) {
                if (!isCaseSensitive && itemString.toLowerCase().startsWith(input.toLowerCase()))
                    return itemString;
                if (isCaseSensitive && itemString.startsWith(input))
                    return itemString;
            }
        }

        return null;
    }

    public void replaceSelection(String s)
    {
        final AutoDocument autoDocument = (AutoDocument) getDocument();
        if (autoDocument != null) {
            try {
                int i = Math.min(getCaret().getDot(), getCaret().getMark());
                int j = Math.max(getCaret().getDot(), getCaret().getMark());
                autoDocument.replace(i, j - i, s, null);
            } catch (Exception exception) {
            }
        }
    }

    public boolean isCaseSensitive()
    {
        return isCaseSensitive;
    }

    public void setCaseSensitive(boolean flag)
    {
        isCaseSensitive = flag;
    }

    public boolean isStrict()
    {
        return isStrict;
    }

    public void setStrict(boolean flag)
    {
        isStrict = flag;
    }

    public List<?> getDataList()
    {
        return dataList;
    }

    public void setDataList(List<?> list)
    {
        if (list == null) {
            throw new IllegalArgumentException("values can not be null");
        } else {
            dataList = list;
            return;
        }
    }
}
