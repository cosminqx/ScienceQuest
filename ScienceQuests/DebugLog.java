public final class DebugLog
{
    public static final boolean DEBUG = false;

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
