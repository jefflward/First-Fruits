package us.wardware.firstfruits;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GivingRecord implements Comparable<GivingRecord>
{
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
    private String dateString;
    private String lastName;
    private String firstName;
    private String fundType;
    private String checkNumber;
    private Map<String, Double> categorizedAmounts;
    private Date date;

    public GivingRecord()
    {
        categorizedAmounts = new HashMap<String, Double>();
    }

    public GivingRecord(String dateString, String lastName, String firstName, String fundType, String checkNumber)
    {
        this.dateString = dateString;
        try {
            this.date = SDF.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Record has invalid date string " + dateString);
        }
        this.lastName = lastName;
        this.firstName = firstName;
        this.fundType = fundType;
        this.checkNumber = checkNumber;
        categorizedAmounts = new HashMap<String, Double>();
    }

    public String getDateString()
    {
        return dateString;
    }
    
    public Date getDate()
    {
        return date;
    }

    public void setDateString(String dateString)
    {
        this.dateString = dateString;
        try {
            this.date = SDF.parse(dateString);
        } catch (ParseException e) {
            System.err.println("Invalid date string " + dateString);
        }
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
    
    public String getFundType()
    {
        return fundType;
    }
    
    public void setFundType(String fundType)
    {
        this.fundType = fundType; 
    }
    
    public String getCheckNumber()
    {
        return checkNumber;
    }
    
    public void setCheckNumber(String checkNumber)
    {
        this.checkNumber = checkNumber; 
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
        setDateString(record.getDateString());
        setLastName(record.getLastName());
        setFirstName(record.getFirstName());
        setFundType(record.getFundType());
        setCheckNumber(record.getCheckNumber());
        categorizedAmounts = new HashMap<String, Double>(record.categorizedAmounts);
    }

    public String toBasicString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(dateString + ", ");
        sb.append(lastName + ", ");
        sb.append(firstName + ", ");
        sb.append(fundType + ", ");
        sb.append(checkNumber);

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
        sb.append("dateString=" + dateString);
        sb.append(", lastName=" + lastName);
        sb.append(", firstName=" + firstName);
        sb.append(", fundType=" + fundType);
        sb.append(", checkNumber=" + checkNumber);

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
        sb.append(dateString);
        sb.append("," + lastName);
        sb.append("," + firstName);
        sb.append("," + fundType);
        sb.append("," + checkNumber);

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

    public static GivingRecord fromCsv(String csv, String[] headers) throws ParseException
    {
        try {
            final String[] tokens = csv.split(",");
            int tokenIndex = 0;
            final String dateString = tokens[tokenIndex++].trim();
            final String lastName = tokens[tokenIndex++].trim();
            final String firstName = tokens[tokenIndex++].trim();
            final String fundType = tokens[tokenIndex++].trim();
            final String checkNumber = tokens[tokenIndex++].trim();
            final GivingRecord record = new GivingRecord(dateString, lastName, firstName, fundType, checkNumber);
            final String[] categories = Arrays.copyOfRange(headers, tokenIndex, headers.length - 1);
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
        if (dateString.equals(other.dateString)) {
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
        result = prime * result + ((dateString == null) ? 0 : dateString.hashCode());
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
        if (dateString == null) {
            if (other.dateString != null)
                return false;
        } else if (!dateString.equals(other.dateString))
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

    public void renameCategory(String oldName, String newName)
    {
        categorizedAmounts.put(newName, categorizedAmounts.get(oldName));
        removeCategory(oldName);
    }

    public void removeCategory(String category)
    {
        categorizedAmounts.remove(category);
    }
}
