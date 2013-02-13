package com.wardware.givingtracker;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GivingRecord implements Comparable<GivingRecord>
{
    private String date;
    private String name;
    private Map<String, Double> categorizedAmounts;
    private Double total;

    public GivingRecord()
    {
        categorizedAmounts = new HashMap<String, Double>();
    }

    public GivingRecord(String date, String name)
    {
        this.date = date;
        this.name = name;
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
        setName(record.getName());
        categorizedAmounts = new HashMap<String, Double>(record.categorizedAmounts);
    }

    public String toBasicString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date + ", ");
        sb.append(name);

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
        sb.append(", name=" + name);

        for (String category : categorizedAmounts.keySet()) {
            sb.append(", " + category + ": ");
            sb.append(NumberFormat.getCurrencyInstance().format(categorizedAmounts.get(category)));
        }
        sb.append(", total=" + NumberFormat.getCurrencyInstance().format(total) + "]");
        return sb.toString();
    }

    public String toCsv()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date);
        sb.append(", " + name);

        for (String category : Settings.getInstance().getCategories()) {
            if (categorizedAmounts.containsKey(category)) {
                sb.append(", " + categorizedAmounts.get(category));
            } else {
                sb.append(", ");
            }
        }
        sb.append(", " + total);
        return sb.toString();
    }

    public String toReportCsv()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(date);
        for (String category : categorizedAmounts.keySet()) {
            sb.append(", " + NumberFormat.getCurrencyInstance().format(categorizedAmounts.get(category)));
        }
        sb.append(", " + total);
        return sb.toString();
    }

    public static GivingRecord fromCsv(String csv, String[] headers) throws ParseException
    {
        try {
            final String[] tokens = csv.split(",");
            int tokenIndex = 0;
            final String date = tokens[tokenIndex++].trim();
            final String name = tokens[tokenIndex++].trim();
            final GivingRecord record = new GivingRecord(date, name);
            final String[] categories = Arrays.copyOfRange(headers, 2, headers.length - 1);
            for (String category : categories) {
                record.setAmountForCategory(category, Double.parseDouble(tokens[tokenIndex++]));
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
            return name.compareTo(other.name);
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
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((total == null) ? 0 : total.hashCode());
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
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (total == null) {
            if (other.total != null)
                return false;
        } else if (!total.equals(other.total))
            return false;
        return true;
    }
}
