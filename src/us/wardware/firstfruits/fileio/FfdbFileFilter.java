package us.wardware.firstfruits.fileio;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class FfdbFileFilter extends FileFilter
{
    @Override
    public boolean accept(File f)
    {
        if (f.isDirectory()) {
            return true;
        }
        final String extension = FileUtils.getExtension(f);
        if (extension != null && extension.equals(FileUtils.FFDB)) {
            return true;
        }

        return false;
    }

    @Override
    public String getDescription()
    {
        return "First Fruits Database (*.ffdb)";
    }
}
