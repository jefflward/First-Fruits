package com.wardware.givingtracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class RecordFilter
{
    private static final String DATE_TYPE = "Date";
    private static final String LAST_NAME_TYPE = "Last Name";
    private static final String FIRST_NAME_TYPE = "First Name";
    private static final String TOTAL_TYPE = "Total";
    
    private static final String EQUAL_OPERATION = "=";
    private static final String GREATER_THAN_OPERATION = ">";
    private static final String LESS_THAN_OPERATION = "<";
    
    private String type;
    private String operation;
    private String value;
    private List<String> nonCategoryTypes;
    private Date dateValue;
    private Double doubleValue;
    
    public RecordFilter()
    {
        type = "";
        operation = EQUAL_OPERATION;
        value = "";
        
        nonCategoryTypes = new ArrayList<String>();
        nonCategoryTypes.add(DATE_TYPE);
        nonCategoryTypes.add(LAST_NAME_TYPE);
        nonCategoryTypes.add(FIRST_NAME_TYPE);
        nonCategoryTypes.add(TOTAL_TYPE);
    }
    
    public boolean isEnabled()
    {
        return type != null && !type.isEmpty();
    }
    
    public boolean isMatch(GivingRecord record)
    {
        final List<String> categories = Settings.getInstance().getCategories();
        if (nonCategoryTypes.contains(type)) {
            if (type.equals(DATE_TYPE)) {
                return isDateMatch(record.getDateString());
            } else if (type.equals(LAST_NAME_TYPE)) {
                return isStringMatch(record.getLastName());
            } else if (type.equals(FIRST_NAME_TYPE)) {
                return isStringMatch(record.getFirstName());
            } else if (type.equals(TOTAL_TYPE)) {
                return isDoubleMatch(record.getTotal());
            }
        } else if (categories.contains(type)) {
            final Double amountForCategory = record.getAmountForCategory(type);
            return isDoubleMatch(amountForCategory);
        }
        return false;
    }
    
    private boolean isDateMatch(String dateString)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            if (dateValue != null) {
                final Date recordDate = sdf.parse(dateString);
                if (operation.equals(EQUAL_OPERATION)) {
                    return recordDate.equals(dateValue);
                } else if (operation.equals(GREATER_THAN_OPERATION)) {
                    return recordDate.after(dateValue);
                } else if (operation.equals(LESS_THAN_OPERATION)) {
                    return recordDate.before(dateValue);
                }
            }
        } catch (ParseException e) {
        }
        return false;
    }

    private boolean isDoubleMatch(Double recordValue)
    {
        if (doubleValue != null) {
            if (operation.equals(EQUAL_OPERATION)) {
                return recordValue.equals(doubleValue);
            } else if (operation.equals(GREATER_THAN_OPERATION)) {
                return recordValue > doubleValue;
            } else if (operation.equals(LESS_THAN_OPERATION)) {
                return recordValue < doubleValue;
            }
        }
        return false;
    }
    
    private boolean isStringMatch(String recordValue)
    {
        String resizedRecordValue = recordValue;
        if (recordValue.length() > value.length()) {
            if (value.length() > 0) {
                resizedRecordValue = recordValue.substring(0, value.length());
            }
        } else {
            resizedRecordValue = StringUtils.rightPad(recordValue, value.length() - recordValue.length());
        }
        if (operation.equals(EQUAL_OPERATION)) {
            return resizedRecordValue.toLowerCase().equals(value.toLowerCase());
        } else if (operation.equals(GREATER_THAN_OPERATION)) {
            return resizedRecordValue.toLowerCase().compareTo(value.toLowerCase()) > 0;
        } else if (operation.equals(LESS_THAN_OPERATION)) {
            return resizedRecordValue.toLowerCase().compareTo(value.toLowerCase()) < 0;
        }
        return false;
    }
    
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            doubleValue = null;
        }
        this.dateValue = null;
    }
    
    public Date getDateValue()
    {
        return dateValue; 
    }

    public void setDateValue(Date date)
    {
        this.dateValue = date;
        this.value = "";
    }

    public boolean hasValue()
    {
        return (value != null && !value.isEmpty()) || dateValue != null;
    }
}
