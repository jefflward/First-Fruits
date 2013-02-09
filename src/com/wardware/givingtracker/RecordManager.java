package com.wardware.givingtracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public class RecordManager extends Observable
{
    private static RecordManager INSTANCE = new RecordManager();
    private List<GivingRecord> records;
    private Set<String> uniqueNames;
    private GivingRecord selectedRecord;
    private boolean unsavedChanges;
    private int selectionCount;
    private GivingRecord lastUpdated;
    private String selectedDate;
    private boolean filterByDate;

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
        uniqueNames = new HashSet<String>();
        uniqueNames.add("");
        records = new ArrayList<GivingRecord>();
        unsavedChanges = false;
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
            if (!uniqueNames.contains(record.getName())) {
                uniqueNames.add(record.getName());
                setChanged();
                notifyObservers(uniqueNames);
            }
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

    public List<String> getUniqueNames()
    {
        final List<String> namesSorted = new ArrayList<String>(uniqueNames);
        Collections.sort(namesSorted);
        return namesSorted;
    }

    public void setUniqueNames(Set<String> names)
    {
        uniqueNames = names;
        setChanged();
        notifyObservers(uniqueNames);
    }

    public void setRecords(List<GivingRecord> records)
    {
        this.records = records;
        final Set<String> names = new HashSet<String>();
        names.add("");
        for (GivingRecord record : records) {
            names.add(record.getName());
        }
        setUniqueNames(names);
        setChanged();
        notifyObservers(records);
    }

    public List<GivingRecord> getAllRecords()
    {
        return records;
    }

    public List<GivingRecord> getRecords()
    {
        if (filterByDate) {
            final List<GivingRecord> recordsForSelectedDate = new ArrayList<GivingRecord>();
            for (GivingRecord record : records)
            {
                if (selectedDate != null && selectedDate.equals(record.getDate())) {
                    recordsForSelectedDate.add(record);
                }
            }
            return recordsForSelectedDate;
        }
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

    public List<GivingRecord> getRecordsForName(String selectedName)
    {
        final List<GivingRecord> recordsForName = new ArrayList<GivingRecord>();
        for (GivingRecord record : records) {
            if (record.getName().equals(selectedName)) {
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

    public void setFilterByDate(boolean filterByDate)
    {
        this.filterByDate = filterByDate;
        setChanged();
        this.notifyObservers(filterByDate);
    }
}
