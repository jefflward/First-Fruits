package com.wardware.givingtracker.fileio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import com.wardware.givingtracker.GivingRecord;
import com.wardware.givingtracker.Settings;

public class ReportWriter
{
    public static final String HEADER_CSV = "Date, General, Missions, Building, Total";
    
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
        printWriter.println(HEADER_CSV);
        double totalGeneral = 0;
        double totalMissions = 0;
        double totalBuilding = 0;
        for (GivingRecord record : records) {
            printWriter.println(record.toReportCsv());
            totalGeneral += record.getGeneral();
            totalMissions += record.getMissions();
            totalBuilding += record.getBuilding();
        }
        final double totalGiving = totalGeneral + totalMissions + totalBuilding;
        printWriter.println("Total," + totalGeneral + ", " + totalMissions + ", " + totalBuilding + ", " + totalGiving);
        printWriter.println();
        printWriter.println("Prepared by,_________________________");
        printWriter.flush();
        printWriter.close();
    }
}
