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

            final String[] tokens = firstLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
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

            // This is the old format
            String[] headers = {"Date","Name","General", "Missions", "Building", "Total"};
            if (tokens[0].equals("Date")) {
                headers = tokens;
            }
            
            String line;
            while ((line = br.readLine()) != null)   {
                final GivingRecord record = GivingRecord.fromCsv(line, headers);
                records.add(record);
            }
        } finally {
           in.close();
           br.close();
        }
        return records;
    }
}
