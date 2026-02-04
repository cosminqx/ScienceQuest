/**
 * PlayerData - Stores global player information
 * Accessible throughout the game to retrieve player name and gender
 */
public class PlayerData
{
    private static String playerName = "";
    private static Gender playerGender = null;

    /**
     * Set player name
     */
    public static void setPlayerName(String name)
    {
        playerName = name;
    }

    /**
     * Get player name
     */
    public static String getPlayerName()
    {
        return playerName;
    }

    /**
     * Set player gender
     */
    public static void setPlayerGender(Gender gender)
    {
        playerGender = gender;
    }

    /**
     * Get player gender
     */
    public static Gender getPlayerGender()
    {
        return playerGender;
    }

    /**
     * Reset player data
     */
    public static void reset()
    {
        playerName = "";
        playerGender = null;
    }
}
