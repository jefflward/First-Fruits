package us.wardware.firstfruits;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Observable;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;

public class Settings extends Observable
{
    public static final String CHURCH_NAME_KEY = "ChurchName"; 
    public static final String CHURCH_ADDRESS_KEY = "ChurchAddress";
    public static final String CATEGORIES_KEY = "Categories";
    public static final String ADDRESS1 = "Address1";
    public static final String ADDRESS2 = "Address2";
    public static final String CITY = "City";
    public static final String STATE = "State";
    public static final String ZIP = "Zip";
    public static final String PHONE = "Phone";
    public static final String INSTALL = "InstallTime";
    public static final String REGISTRATION_KEY = "Registration";
    public static final String REGISTRATION_NAME = "RegistrationName";
    public static final String RECENT_FILES_KEY = "RecentFiles";
    
    private static Settings INSTANCE;
    private Preferences preferences;
    
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
        preferences = Preferences.userNodeForPackage(this.getClass());
    }
    
    public String getInstallDate()
    {
        return preferences.get(INSTALL, null);
    }
    
    public void setInstallDate(String installDate)
    {
        preferences.put(INSTALL, installDate); 
    }

    public List<String> getCategories()
    {
        final List<String> categories = new ArrayList<String>();
        final String categoriesProperty = preferences.get(Settings.CATEGORIES_KEY, null);
        if (categoriesProperty != null) {
            categories.addAll(Arrays.asList(categoriesProperty.split(";")));
        } else {
            categories.add("General");
            categories.add("Missions");
            categories.add("Building");
            preferences.put(Settings.CATEGORIES_KEY, StringUtils.join(categories, ";"));
        }
        return categories;
    }
    
    public void addCategory(String category)
    {
        final List<String> categories = getCategories();
        categories.add(category.trim());
        preferences.put(Settings.CATEGORIES_KEY, StringUtils.join(categories, ";"));
        setChanged();
        notifyObservers();
    }
    
    public String getStringValue(String key)
    {
        return preferences.get(key, "");
    }

    public void setStringValue(String key, String value)
    {
        preferences.put(key, value);
        setChanged();
        notifyObservers();
    }

    public String getRegistrationKey()
    {
        return getStringValue(REGISTRATION_KEY);
    }

    public void setRegistrationKey(String key)
    {
        preferences.put(REGISTRATION_KEY, key.trim()); 
    }

    public String getRegistrationName()
    {
        return getStringValue(REGISTRATION_NAME);
    }
    
    public void setRegistrationName(String name)
    {
        preferences.put(REGISTRATION_NAME, name.trim()); 
    }

    public Deque<String> getRecentFiles()
    {
        final Deque<String> recentFiles = new ArrayDeque<String>();
        
        final String files = preferences.get(Settings.RECENT_FILES_KEY, null);
        if (files != null) {
            recentFiles.addAll(Arrays.asList(files.split("\\|")));
        }
        return recentFiles;
    }
    
    public void addRecentFile(String filePath)
    {
        final Deque<String> recentFiles = getRecentFiles();
        if (recentFiles.contains(filePath)) {
            recentFiles.remove(filePath);
        }
        recentFiles.addFirst(filePath.trim());
        
        while (recentFiles.size() > 10) {
            recentFiles.removeLast();
        }
            
        final String filesJoined = StringUtils.join(recentFiles, "|");
        preferences.put(Settings.RECENT_FILES_KEY, filesJoined);
        setChanged();
        notifyObservers();
    }
}