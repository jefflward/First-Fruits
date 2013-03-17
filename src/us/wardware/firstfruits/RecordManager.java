package us.wardware.firstfruits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class RecordManager extends Observable implements Observer
{
    private static RecordManager INSTANCE = new RecordManager();
    private List<GivingRecord> records;
    private Set<String> uniqueLastNames;
    private Map<String, Set<String>> firstNamesForLastName;
    private GivingRecord selectedRecord;
    private boolean unsavedChanges;
    private int selectionCount;
    private GivingRecord lastUpdated;
    private String selectedDate;
    private RecordFilter recordFilter;

    public static RecordManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    private RecordManager()
    {
        uniqueLastNames = new HashSet<String>();
        uniqueLastNames.add("");
        uniqueLastNames.add("Anonymous");
        firstNamesForLastName = new HashMap<String, Set<String>>();
        records = new ArrayList<GivingRecord>();
        unsavedChanges = false;
        recordFilter = new RecordFilter();
        Settings.getInstance().addObserver(this);
    }

    public void updateRecord(GivingRecord record)
    {
        unsavedChanges = true;
        if (selectedRecord != null) {
            selectedRecord.update(record);
            lastUpdated = selectedRecord;
            setChanged();
            notifyObservers(selectedRecord);
        } else {
            lastUpdated = record;
            records.add(record);
            setChanged();
            notifyObservers(records);
            if (!uniqueLastNames.contains(record.getLastName())) {
                uniqueLastNames.add(record.getLastName().trim());
                setChanged();
                notifyObservers(uniqueLastNames);
            }
            
            updateFirstNamesForLastName(record.getLastName(), record.getFirstName());
        }
    }

    public GivingRecord getLastUpdatedRecord()
    {
        return lastUpdated;
    }

    public GivingRecord getSelectedRecord()
    {
        return selectedRecord;
    }

    public void setSelectedRecord(GivingRecord record)
    {
        this.selectedRecord = record;
        setChanged();
        notifyObservers();
    }

    public List<String> getUniqueLastNames()
    {
        final List<String> namesSorted = new ArrayList<String>(uniqueLastNames);
        Collections.sort(namesSorted);
        return namesSorted;
    }

    public void setUniqueNames(Set<String> names)
    {
        uniqueLastNames = names;
        setChanged();
        notifyObservers(uniqueLastNames);
    }

    public void setRecords(List<GivingRecord> records)
    {
        this.records = records;
        final Set<String> names = new HashSet<String>();
        names.add("");
        updateFirstNamesForLastName("", "");
        for (GivingRecord record : records) {
            names.add(record.getLastName().trim());
            updateFirstNamesForLastName(record.getLastName(), record.getFirstName());
        }
        setUniqueNames(names);
        setChanged();
        notifyObservers(records);
    }

    public List<GivingRecord> getAllRecords()
    {
        return records;
    }
    
    public List<GivingRecord> getRecordsForDate(String date)
    {
        final List<GivingRecord> recordsForSelectedDate = new ArrayList<GivingRecord>();
        for (GivingRecord record : records)
        {
            if (date != null && date.equals(record.getDateString())) {
                recordsForSelectedDate.add(record);
            }
        }
        return recordsForSelectedDate;
    }

    public List<GivingRecord> getRecords()
    {
        return records;
    }
    
    public boolean hasUnsavedChanges()
    {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean value)
    {
        unsavedChanges = value;
        setChanged();
        notifyObservers(unsavedChanges);
    }

    public List<GivingRecord> getRecordsForName(String lastName, String firstName)
    {
        final List<GivingRecord> recordsForName = new ArrayList<GivingRecord>();
        for (GivingRecord record : records) {
            if (record.getLastName().equals(lastName) &&
                record.getFirstName().equals(firstName)) {
                recordsForName.add(record);
            }
        }
        return recordsForName;
    }

    public void deleteRecords(List<GivingRecord> toRemove)
    {
        if (records.removeAll(toRemove)) {
            unsavedChanges = true;
            setChanged();
            notifyObservers(records);
        }
    }

    public int getSelectionCount()
    {
        return selectionCount;
    }

    public void setSelectionCount(int selectionCount)
    {
        this.selectionCount = selectionCount;
        setChanged();
        notifyObservers(selectionCount);
    }

    public void createNew()
    {
        records.clear();
        uniqueLastNames.clear();
        uniqueLastNames.add("");
        uniqueLastNames.add("Anonymous");
        firstNamesForLastName.clear();        
        selectedRecord = null;
        selectionCount = 0;
        unsavedChanges = false;
        setChanged();
        notifyObservers(records);
    }

    public void setSelectedDate(String date)
    {
        setChanged();
        selectedDate = date;
        notifyObservers(selectedDate);
    }

    public String getSelectedDate()
    {
        return selectedDate;
    }
    
    public RecordFilter getRecordFilter()
    {
        return recordFilter;
    }
    
    public void setRecordFilter(RecordFilter filter)
    {
        recordFilter = filter;
        setChanged();
        notifyObservers(recordFilter);
    }

    @Override
    public void update(Observable arg0, Object arg1)
    {
        final List<String> categories = Settings.getInstance().getCategories();
        for (GivingRecord record : records) {
            record.updateCategories(categories);
        }
    }

    public List<String> getFirstNamesForLastName(String lastName)
    {
        if (firstNamesForLastName.containsKey(lastName)) {
            final List<String> namesSorted = new ArrayList<String>(firstNamesForLastName.get(lastName));
            Collections.sort(namesSorted);
            return namesSorted;
        }
        return new ArrayList<String>();
    }
    
    public void updateFirstNamesForLastName(String lastName, String firstName)
    {
        if (!firstNamesForLastName.containsKey(lastName)) {
            firstNamesForLastName.put(lastName, new HashSet<String>());
        }

        firstNamesForLastName.get(lastName).add(firstName);
    }

    public List<String> getReportNameList()
    {
        final List<String> reportNames = new ArrayList<String>();
        reportNames.add("");
        for (String lastName : firstNamesForLastName.keySet()) {
            if (!lastName.isEmpty()) {
                for (String firstName : firstNamesForLastName.get(lastName)) {
                    reportNames.add(lastName + ", " + firstName);
                }
            }
        }
        return reportNames;
    }
}
