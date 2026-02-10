public final class DebugLog
{
    public static final boolean DEBUG = Boolean.parseBoolean(
        System.getProperty("sciencequest.debug", "true")
    );

    private DebugLog()
    {
    }

    public static void log(String message)
    {
        if (DEBUG)
        {
            System.out.println(message);
        }
    }
}
