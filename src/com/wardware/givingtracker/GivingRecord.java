package com.wardware.givingtracker;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GivingRecord implements Comparable<GivingRecord>
{
    private String date;
    private String lastName;
    private String firstName;
    private Map<String, Double> categorizedAmounts;

    public GivingRecord()
    {
        categorizedAmounts = new HashMap<String, Double>();
    }

    public GivingRecord(String date, String lastName, String firstName)
    {
        this.date = date;
        this.lastName = lastName;
        this.firstName = firstName;
        categorizedAmounts = new HashMap<String, Double>();
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String name)
    {
        this.lastName = name;
    }
    
    public String getFirstName()
    {
        return firstName;
    }
    
    public void setFirstName(String name)
    {
        this.firstName = name;
    }

    public Double getAmountForCategory(String category)
    {
        if (categorizedAmounts.containsKey(category)) {
            return categorizedAmounts.get(category);
        }
        return 0.0;
    }

    public void setAmountForCategory(String category, Double amount)
    {
        categorizedAmounts.put(category, amount);
    }

    public Double getTotal()
    {
        Double total = 0.0;
        for (Double value : categorizedAmounts.values()) {
            total += value;
        }
        return total;
    }

    public void update(GivingRecord record)
    {
        setDate(record.getDate());
        setLastName(record.getLastName());
        setFirstName(record.getLastName());
        categorizedAmounts = new HashMap<String, Double>(record.categorizedAmounts);
    }

    public String toBasicString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date + ", ");
        sb.append(lastName + ", ");
        sb.append(firstName);

        for (String category : categorizedAmounts.keySet()) {
            sb.append(", " + category + ": ");
            sb.append(NumberFormat.getCurrencyInstance().format(categorizedAmounts.get(category)));
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("GivingRecord [");
        sb.append("date=" + date);
        sb.append(", lastName=" + lastName);
        sb.append(", firstName=" + firstName);

        for (String category : categorizedAmounts.keySet()) {
            sb.append(", " + category + ": ");
            sb.append(NumberFormat.getCurrencyInstance().format(categorizedAmounts.get(category)));
        }
        sb.append(", total=" + NumberFormat.getCurrencyInstance().format(getTotal()) + "]");
        return sb.toString();
    }

    public String toCsv()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date);
        sb.append("," + lastName);
        sb.append("," + firstName);

        for (String category : Settings.getInstance().getCategories()) {
            if (categorizedAmounts.containsKey(category)) {
                sb.append("," + categorizedAmounts.get(category));
            } else {
                sb.append(",0.0");
            }
        }
        sb.append("," + getTotal());
        return sb.toString();
    }

    public String toReportCsv()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date);
        for (String category : categorizedAmounts.keySet()) {
            sb.append("," + NumberFormat.getCurrencyInstance().format(categorizedAmounts.get(category)));
        }
        sb.append("," + getTotal());
        return sb.toString();
    }

    public static GivingRecord fromCsv(String csv, String[] headers) throws ParseException
    {
        try {
            final String[] tokens = csv.split(",");
            int tokenIndex = 0;
            final String date = tokens[tokenIndex++].trim();
            final String lastName = tokens[tokenIndex++].trim();
            final String firstName = tokens[tokenIndex++].trim();
            final GivingRecord record = new GivingRecord(date, lastName, firstName);
            final String[] categories = Arrays.copyOfRange(headers, 3, headers.length - 1);
            final List<String> definedCategories = Settings.getInstance().getCategories();
            for (String category : categories) {
                if (definedCategories.contains(category)) {
                    record.setAmountForCategory(category, Double.parseDouble(tokens[tokenIndex++]));
                }
            }
            return record;
        } catch (Exception e) {
            throw new ParseException(csv, 0);
        }
    }

    @Override
    public int compareTo(GivingRecord other)
    {
        if (date.equals(other.date)) {
            return lastName.compareTo(other.lastName);
        }
        return date.compareTo(other.date);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((categorizedAmounts == null) ? 0 : categorizedAmounts.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GivingRecord other = (GivingRecord) obj;
        if (categorizedAmounts == null) {
            if (other.categorizedAmounts != null)
                return false;
        } else if (!categorizedAmounts.equals(other.categorizedAmounts))
            return false;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        return true;
    }

    public void updateCategories(List<String> categories)
    {
        final Set<String> keys = new HashSet<String>(categorizedAmounts.keySet());
        keys.removeAll(categories);
        for (String key : keys) {
            categorizedAmounts.remove(key);
        }
    }
}
