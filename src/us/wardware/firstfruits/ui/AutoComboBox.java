package us.wardware.firstfruits.ui;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboBoxEditor;

public class AutoComboBox extends JComboBox<String>
{
    private AutoTextFieldEditor autoTextFieldEditor;
    private boolean isFired;
    
    private class AutoTextFieldEditor extends BasicComboBoxEditor {
        private AutoTextField getAutoTextFieldEditor() {
            return (AutoTextField) editor;
        }

        AutoTextFieldEditor(List<?> list) {
            editor = new AutoTextField(list, AutoComboBox.this);
            editor.addFocusListener(new FocusAdapter(){
                @Override
                public void focusLost(FocusEvent e) {
                    isFired = false;
                    fireActionEvent();
                }
            });
        }
    }

    public AutoComboBox(List<String> list)
    {
        isFired = false;
        autoTextFieldEditor = new AutoTextFieldEditor(list);
        setEditable(true);
        setModel(new DefaultComboBoxModel<String>(list.toArray(new String[list.size()])));
        setEditor(autoTextFieldEditor);
    }

    public boolean isCaseSensitive()
    {
        return autoTextFieldEditor.getAutoTextFieldEditor().isCaseSensitive();
    }

    public void setCaseSensitive(boolean flag)
    {
        autoTextFieldEditor.getAutoTextFieldEditor().setCaseSensitive(flag);
    }

    public boolean isStrict()
    {
        return autoTextFieldEditor.getAutoTextFieldEditor().isStrict();
    }

    public void setStrict(boolean flag)
    {
        autoTextFieldEditor.getAutoTextFieldEditor().setStrict(flag);
    }

    public List<?> getDataList()
    {
        return autoTextFieldEditor.getAutoTextFieldEditor().getDataList();
    }

    public void setDataList(List<String> list)
    {
        autoTextFieldEditor.getAutoTextFieldEditor().setDataList(list);
        setModel(new DefaultComboBoxModel<String>(list.toArray(new String[list.size()])));
    }

    void setSelectedValue(Object obj)
    {
        if (isFired) {
            return;
        } else {
            isFired = true;
            setSelectedItem(obj);
            fireItemStateChanged(new ItemEvent(this, 701, selectedItemReminder, 1));
            isFired = false;
            return;
        }
    }

    protected void fireActionEvent()
    {
        if (!isFired)
            super.fireActionEvent();
    }
}
