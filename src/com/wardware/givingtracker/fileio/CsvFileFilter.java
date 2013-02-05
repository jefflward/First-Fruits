package com.wardware.givingtracker.fileio;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CsvFileFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory()) {
            return true;
        }
        final String extension = FileUtils.getExtension(f);
        if (extension != null && extension.equals(FileUtils.CSV)) {
            return true;
        }

        return false;
    }

    @Override
    public String getDescription()
    {
        return "Just CSV files";
    }
}
