/**
 * NPC Interface - Base interface for all NPC characters in the game
 * Defines the contract for NPCs that can have dialogue
 */
public interface NPC
{
    /**
     * Get the dialogue text that this NPC should display
     * @param playerName The name of the player
     * @return The dialogue text
     */
    String getDialogueText(String playerName);
    
    /**
     * Get the icon path for this NPC's dialogue box
     * @return Path to the icon image
     */
    String getIconPath();
}
