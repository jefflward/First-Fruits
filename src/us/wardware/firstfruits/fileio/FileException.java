package us.wardware.firstfruits.fileio;

public class FileException extends Exception
{
    public FileException(String message)
    {
        super(message);
    }
    
    public FileException(Throwable throwable)
    {
        super(throwable);
    } 
    
    public FileException(String message, Throwable throwable)
    {
        super(message, throwable);
    } 
}
