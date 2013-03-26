package us.wardware.firstfruits.fileio;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
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
    
    public static CategoryComparison getCategoryComparison(File file, boolean encrypted) throws IOException, FileException
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
            
            String schemaLine = firstLine;
            if (encrypted) {
                schemaLine = FileEncryption.decrypt(firstLine);
            }

            String[] tokens = schemaLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
            }

            if (tokens[0].equals(SchemaSettings.SCHEMA_VERSION_KEY)) {
                String headerLine = br.readLine();
                if (encrypted) {
                    headerLine = FileEncryption.decrypt(headerLine);
                }
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
        } catch (GeneralSecurityException e) {
            throw new FileException("Could not decrypt file: " + file.getName(), e);
        } finally {
           in.close();
           br.close();
        }
    }
    
    public static Set<GivingRecord> readRecordsFromFile(File file, boolean encrypted) throws IOException, ParseException, FileException
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
            
            String schemaLine = firstLine;
            if (encrypted) {
                schemaLine = FileEncryption.decrypt(firstLine);
            }

            final String[] tokens = schemaLine.split(",");
            if (tokens.length == 0) {
                throw new RuntimeException(String.format("The file: %f is corrupt.", file.getName()));
            }

            final String schemaVersion;
            final String[] headers;
            if (tokens[0].equals(SchemaSettings.SCHEMA_VERSION_KEY)) {
                schemaVersion = tokens[1];
                String headerLine = br.readLine();
                if (encrypted) {
                    headerLine = FileEncryption.decrypt(headerLine);
                }
                headers = headerLine.split(",");
            } else if (tokens[0].equals("Date")) {
                schemaVersion = SchemaSettings.VERSION_1_0;
                headers = tokens;
            } else {
                throw new ParseException("Unable to determine file format", 0);
            }
            
            String line;
            while ((line = br.readLine()) != null)   {
                if (encrypted) {
                    line = FileEncryption.decrypt(line);
                }
                final GivingRecord record = fromCsv(line, schemaVersion, headers);
                records.add(record);
            }
        } catch (GeneralSecurityException e) {
            throw new FileException("Could not decrypt file: " + file.getName(), e);
        } finally {
           in.close();
           br.close();
        }
        return records;
    }
    
    public static String getFilePassword(File file) throws FileException, FileNotFoundException 
    {
        FileInputStream fstream;
        fstream = new FileInputStream(file);
        final DataInputStream in = new DataInputStream(fstream);
        final BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try 
        {
            final String firstLine = br.readLine();
            if (firstLine == null || firstLine.isEmpty()) {
                throw new FileException(String.format("The file: %f is missing header.", file.getName()));
            }

            if (firstLine.startsWith(SchemaSettings.SCHEMA_VERSION_KEY) || firstLine.startsWith("Date")) {
                // Not encrypted
                return "";
            }
            
            final String decrypted = FileEncryption.decrypt(firstLine);
            
            final String[] tokens = decrypted.split(",");
            if (tokens.length == 0 || tokens.length != SchemaSettings.SCHEMA_TOKEN_LENGTH) {
                throw new FileException(String.format("Unable to determine file format. The file: %f is corrupt.", file.getName()));
            }
            final String password = tokens[3];
            return password;
        } catch (GeneralSecurityException e) {
            throw new FileException("Could not decrypt file: " + file.getName(), e);
        } catch (IOException e) {
            throw new FileException("Failed to read file: " + file.getName(), e);
        } finally {
            try {
                in.close();
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
