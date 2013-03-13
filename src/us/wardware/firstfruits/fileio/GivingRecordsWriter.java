package us.wardware.firstfruits.fileio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import us.wardware.firstfruits.GivingRecord;
import us.wardware.firstfruits.RecordManager;
import us.wardware.firstfruits.Settings;


public class GivingRecordsWriter
{
    public static void writeRecordsToFile(File outputFile) throws IOException
    {
        final FileWriter fileWriter = new FileWriter(outputFile);
        final PrintWriter printWriter = new PrintWriter(fileWriter);
        final List<String> columns = new ArrayList<String>();
        columns.add("Date");
        columns.add("Last Name");
        columns.add("First Name");
        columns.add("Fund Type");
        columns.add("Check Number");
        columns.addAll(Settings.getInstance().getCategories());
        columns.add("Total");
        printWriter.println(SchemaSettings.getCurrentSchemaVersionCsv());
        final String columnsCsv = StringUtils.join(columns.toArray(), ",");
        printWriter.println(columnsCsv);
        final List<GivingRecord> records = RecordManager.getInstance().getAllRecords();
        for (GivingRecord record : records) {
            printWriter.println(record.toCsv());
        }
        printWriter.flush();
        printWriter.close();
    }
}
