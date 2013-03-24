package us.wardware.firstfruits.fileio;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class SchemaSettings
{
    public static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    
    public static String SCHEMA_VERSION_KEY = "SchemaVersion";
    public static String CURRENT_SCHEMA_VERSION = "1.1";
    public static int SCHEMA_TOKEN_LENGTH = 4;

    public static String VERSION_1_0 = "1.0";
    public static String VERSION_1_1 = "1.1";

    public static String getCurrentSchemaVersionCsv(String password)
    {
        final String schemaCsv = SCHEMA_VERSION_KEY + "," + CURRENT_SCHEMA_VERSION + "," + sdf.format(new Date());
        if (!password.isEmpty()) {
            return schemaCsv + "," + password;
        }
        return schemaCsv;
    }
}
