package us.wardware.firstfruits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;

public class Settings extends Observable
{
    public static final String ORGANIZATION_NAME_KEY = "OrganizationName"; 
    public static final String ORGANIZATION_ADDRESS_KEY = "OrganizationAddress";
    public static final String CATEGORIES_KEY = "Categories";
    public static final String ADDRESS1 = "Address1";
    public static final String ADDRESS2 = "Address2";
    public static final String CITY = "City";
    public static final String STATE = "State";
    public static final String ZIP = "Zip";
    public static final String PHONE = "Phone";
    
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
    
    public List<String> getCategories()
    {
        final List<String> categories = new ArrayList<String>();
        final String categoriesProperty = preferences.get(Settings.CATEGORIES_KEY, null);
        if (categoriesProperty != null) {
            categories.addAll(Arrays.asList(categoriesProperty.split(";")));
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
}