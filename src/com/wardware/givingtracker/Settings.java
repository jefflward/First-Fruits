package com.wardware.givingtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Properties;

import javax.swing.JOptionPane;

public class Settings extends Observable
{
    public static final String ORGANIZATION_NAME_KEY = "OrganizationName"; 
    public static final String ORGANIZATION_ADDRESS_KEY = "OrganizationAddress";
    public static final String CATEGORIES_KEY = "Categories";
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
    }
    
    public List<String> getCategories()
    {
        final List<String> categories = new ArrayList<String>();
        final String categoriesProperty = properties.getProperty(Settings.CATEGORIES_KEY);
        if (categoriesProperty != null) {
            categories.addAll(Arrays.asList(categoriesProperty.split(";")));
        }
        return categories;
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
        setChanged();
        notifyObservers();
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
}