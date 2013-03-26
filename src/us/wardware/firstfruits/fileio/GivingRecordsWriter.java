package us.wardware.firstfruits.fileio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;


public class GivingRecordsWriter
{
    public static void writeRecordsToFile(File outputFile, String password) throws FileException
    {
        boolean encrypt = (password != null && !password.isEmpty());
        
        try {
            FileWriter fileWriter = new FileWriter(outputFile);
            final PrintWriter printWriter = new PrintWriter(fileWriter);
            final List<String> columns = new ArrayList<String>();
            columns.add("Date");
            columns.add("Last Name");
            columns.add("First Name");
            columns.add("Fund Type");
            columns.add("Check Number");
            columns.addAll(Settings.getInstance().getCategories());
            columns.add("Total");
            
            writeLine(printWriter, SchemaSettings.getCurrentSchemaVersionCsv(password), encrypt);

            final String columnsCsv = StringUtils.join(columns.toArray(), ",");
            writeLine(printWriter, columnsCsv, encrypt);
            final List<GivingRecord> records = RecordManager.getInstance().getAllRecords();
            for (GivingRecord record : records) {
                writeLine(printWriter, record.toCsv(), encrypt);
            }

            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            throw new FileException("Failure occurred while writing file", e);
        }
    }
    
    private static void writeLine(PrintWriter printWriter, String line, boolean encrypt) throws FileException
    {
        String output = line;
        if (encrypt) {
            try {
                output = FileEncryption.encrypt(line);
            } catch (UnsupportedEncodingException e) {
                throw new FileException("Failure occurred while writing file", e);
            } catch (GeneralSecurityException e) {
                throw new FileException("Failure occurred while writing file", e);
            }
        }
        
        printWriter.println(output);
    }
}
