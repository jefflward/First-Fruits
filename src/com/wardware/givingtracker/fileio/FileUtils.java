package com.wardware.givingtracker.fileio;

import java.io.File;

public class FileUtils
{
    public static final String CSV = "csv";
    public static final String XLSX = "xlsx";
    
    public static String getExtension(File f) {
        String ext = null;
        final String name = f.getName();
        int i = name.lastIndexOf('.');
        
        if (i > 0 &&  i < name.length() - 1) {
            ext = name.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
