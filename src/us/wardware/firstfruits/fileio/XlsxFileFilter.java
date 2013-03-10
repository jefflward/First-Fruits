package us.wardware.firstfruits.fileio;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class XlsxFileFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory()) {
            return true;
        }
        final String extension = FileUtils.getExtension(f);
        if (extension != null && extension.equals(FileUtils.XLSX)) {
            return true;
        }

        return false;
    }

    @Override
    public String getDescription()
    {
        return "Just XLSX files";
    }
}
