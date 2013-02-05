package com.wardware.givingtracker.fileio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.Settings;

public class ReportWriter
{
    public static void writeReport(String name, File outputFile, List<GivingRecord> records) throws IOException
    {
        final FileWriter fileWriter = new FileWriter(outputFile);
        final PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(Settings.getInstance().getProperties().getProperty(Settings.ORGANIZATION_NAME_KEY));
        printWriter.println(Settings.getInstance().getProperties().getProperty(Settings.ORGANIZATION_ADDRESS_KEY));
        printWriter.println("Giving Record " + Calendar.getInstance().get(Calendar.YEAR));
        printWriter.println();
        printWriter.println(name);
        printWriter.println();
        final StringBuilder headerCsv = new StringBuilder();
        headerCsv.append("Date,");
        final List<String> categories = Settings.getInstance().getCategories();
        final Map<String, Double> categoryTotals = new HashMap<String, Double>();
        for (String category : categories) {
            headerCsv.append(category + ",");
            categoryTotals.put(category, 0.0);
        }
        headerCsv.append("Total");
        printWriter.println(headerCsv.toString());
        
        double totalGiving = 0.0;
        for (GivingRecord record : records) {
            printWriter.println(record.toReportCsv());
            for (String category : categories) {
                final Double amount = record.getAmountForCategory(category);
                categoryTotals.put(category, categoryTotals.get(category) + amount);
                totalGiving += amount;
            }
        }
       
        final StringBuilder totals = new StringBuilder();
        totals.append("Total,");
        for (String category : categories) {
            totals.append(categoryTotals.get(category));
            totals.append(",");
        }
        totals.append(totalGiving);
            
        printWriter.println();
        printWriter.println("Prepared by,_________________________");
        printWriter.flush();
        printWriter.close();
    }
}
