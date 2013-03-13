package us.wardware.firstfruits.fileio;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.Settings;


public class GivingRecordsReader
{
    public static class CategoryComparison
    {
        final public List<String> allCategories;
        final public List<String> extraCategories;
        final public List<String> missingCategories;
        
        public CategoryComparison(List<String> all, List<String> extra, List<String> missing)
        {
            allCategories = all;
            extraCategories = extra;
            missingCategories = missing;
        } 
        
        public boolean hasExtraCategories()
        {
            return extraCategories.size() > 0;
        }
    }
    
    public static CategoryComparison getCategoryComparison(File file) throws IOException
    {
        final List<String> categoryColumns = new ArrayList<String>();
        categoryColumns.addAll(Settings.getInstance().getCategories());
        
        final List<String> defaultColumns = new ArrayList<String>();
        defaultColumns.add("Date");
        defaultColumns.add("Last Name");
        defaultColumns.add("First Name");
        defaultColumns.add("Fund Type");
        defaultColumns.add("Check Number");
        defaultColumns.add("Total");
        
        final FileInputStream fstream = new FileInputStream(file);
        final DataInputStream in = new DataInputStream(fstream);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try 
        {
            final String firstLine = br.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                throw new RuntimeException(String.format("The file: %f is missing header.", file.getName()));
            }

            String[] tokens = firstLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
            }
            
            if (tokens[0].equals("SchemaVersion")) {
                final String headerLine = br.readLine();
                tokens = headerLine.split(",");
            }
            
            final List<String> missingColumns = new ArrayList<String>(categoryColumns); 
            missingColumns.removeAll(Arrays.asList(tokens)); 
            final List<String> extraColumns = new ArrayList<String>(Arrays.asList(tokens));
            extraColumns.removeAll(defaultColumns);
            extraColumns.removeAll(categoryColumns);
            final List<String> allColumns = new ArrayList<String>(Arrays.asList(tokens));
            allColumns.removeAll(defaultColumns);
            
            return new CategoryComparison(allColumns, extraColumns, missingColumns);
        } finally {
           in.close();
           br.close();
        }
    }
    
    public static Set<GivingRecord> readRecordsFromFile(File file) throws IOException, ParseException
    {
        final Set<GivingRecord> records = new HashSet<GivingRecord>();
        final FileInputStream fstream = new FileInputStream(file);
        final DataInputStream in = new DataInputStream(fstream);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try 
        {
            final String firstLine = br.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                throw new RuntimeException(String.format("The file: %f is missing header.", file.getName()));
            }

            final String[] tokens = firstLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
            }

            final String schemaVersion;
            final String[] headers;
            if (tokens[0].equals("SchemaVersion")) {
                schemaVersion = tokens[1];
                final String headerLine = br.readLine();
                headers = headerLine.split(",");
            } else if (tokens[0].equals("Date")) {
                schemaVersion = SchemaSettings.VERSION_1_0;
                headers = tokens;
            } else {
                throw new ParseException("Unable to determine file format", 0);
            }
            
            String line;
            while ((line = br.readLine()) != null)   {
                final GivingRecord record = fromCsv(line, schemaVersion, headers);
                records.add(record);
            }
        } finally {
           in.close();
           br.close();
        }
        return records;
    }
    
    public static GivingRecord fromCsv(String csv, String schemaVersion, String[] headers) throws ParseException
    {
        if (schemaVersion.equals(SchemaSettings.VERSION_1_0)) {
            return fromSchemaVersion10Csv(csv, headers);
        } else if (schemaVersion.equals(SchemaSettings.VERSION_1_1)) {
            return fromSchemaVersion11Csv(csv, headers);
        }
        return null;
    }
    
    public static GivingRecord fromSchemaVersion10Csv(String csv, String[] headers) throws ParseException
    {
        try {
            final String[] tokens = csv.split(",");
            int tokenIndex = 0;
            final String dateString = tokens[tokenIndex++].trim();
            final String lastName = tokens[tokenIndex++].trim();
            final String firstName = tokens[tokenIndex++].trim();
            final GivingRecord record = new GivingRecord(dateString, lastName, firstName, "", "");
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
    
    public static GivingRecord fromSchemaVersion11Csv(String csv, String[] headers) throws ParseException
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
}