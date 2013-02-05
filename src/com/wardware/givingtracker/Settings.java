package com.wardware.givingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;

public class Settings implements Observer
{
    public static final String ORGANIZATION_NAME_KEY = "OrganizationName"; 
    public static final String ORGANIZATION_ADDRESS_KEY = "OrganizationAddress";
    public static final String DEFAULT_NAMES_KEY = "DefaultNames";
    private static final String SETTINGS_FILE_NAME = "GivingTracker.props";
    private static Settings INSTANCE;
    private Properties properties;
    
    static {
        INSTANCE = new Settings();
    }
    
    public static Settings getInstance()
    {
        return INSTANCE;
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private Settings()
    {
        properties = new Properties();
        loadSettings();
        RecordManager.getInstance().addObserver(this);
    }

    private void loadSettings()
    {
        final File propsFile = new File(SETTINGS_FILE_NAME);
        if (propsFile.exists()) {
            try {
                getProperties().load(new FileInputStream(propsFile));
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Error occurred while loading settings: " + e.getMessage(), "Load Settings Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error occurred while loading settings: " + e.getMessage(), "Load Settings Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties(Properties props)
    {
        this.properties = props;
        saveSettings();
    }
    
    public void saveSettings()
    {
        try {
            properties.store(new FileOutputStream(new File(SETTINGS_FILE_NAME)), "Properties for GivingTracker");
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error occurred while saving settings: " + e.getMessage(), "Save Settings Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error occurred while saving settings: " + e.getMessage(), "Save Settings Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void update(Observable o, Object value)
    {
        if (value instanceof Set) {
            @SuppressWarnings("unchecked")
            final Set<String> names = (Set<String>) value;
            final List<String> sortedNames = new ArrayList<String>();
            sortedNames.addAll(names);            
            Collections.sort(sortedNames);
            final StringBuilder builder = new StringBuilder();
            for (String name : sortedNames) {
                if (!name.trim().isEmpty()) {
                    builder.append(name);
                    builder.append(";");
                }
            }
            properties.put(Settings.DEFAULT_NAMES_KEY, builder.toString());
            saveSettings();
        }
    }
}