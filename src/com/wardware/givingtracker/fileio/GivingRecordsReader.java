package com.wardware.givingtracker.fileio;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import com.wardware.givingtracker.GivingRecord;

public class GivingRecordsReader
{
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
