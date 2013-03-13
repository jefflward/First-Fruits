package us.wardware.firstfruits.fileio;

public abstract class SchemaSettings
{
    public static String SCHEMA_VERSION_KEY = "SchemaVersion";
    public static String CURRENT_SCHEMA_VERSION = "1.1";

    public static String VERSION_1_0 = "1.0";
    public static String VERSION_1_1 = "1.1";

    public static String getCurrentSchemaVersionCsv()
    {
        return SCHEMA_VERSION_KEY + "," + CURRENT_SCHEMA_VERSION;
    }
}
