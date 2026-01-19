import greenfoot.*;

/**
 * DialogueManager - Manages global dialogue state for the game
 * Prevents multiple dialogue boxes from spawning at the same time
 * Handles input for showing/hiding dialogue
 */
public class DialogueManager
{
    // Singleton instance
    private static DialogueManager instance;
    
    // Current dialogue state
    private DialogueBox currentDialogue;
    private World currentWorld;
    private NPC currentNPC; // Reference to the NPC that triggered the dialogue
    
    /**
     * Private constructor (singleton pattern)
     */
    private DialogueManager()
    {
        currentDialogue = null;
        currentWorld = null;
        currentNPC = null;
    }
    
    /**
     * Get the singleton instance of DialogueManager
     */
    public static DialogueManager getInstance()
    {
        if (instance == null)
        {
            instance = new DialogueManager();
        }
        return instance;
    }
    
    /**
     * Show a dialogue box
     * @param dialogue The DialogueBox to display
     * @param world The world to add the dialogue to
     * @param npc The NPC that triggered this dialogue (for reference)
     * @return true if dialogue was shown, false if already in dialogue
     */
    public boolean showDialogue(DialogueBox dialogue, World world, NPC npc)
    {
        // If already showing dialogue, don't add another
        if (currentDialogue != null)
        {
            System.out.println("DEBUG: Dialogue already active, ignoring new dialogue");
            return false;
        }
        
        currentDialogue = dialogue;
        currentWorld = world;
        currentNPC = npc;
        
        // Position the dialogue box at the bottom center of the screen
        int worldWidth = world.getWidth();
        int centeredX = worldWidth / 2;
        int positionedY = world.getHeight() - 60; // 60 pixels from bottom
        
        world.addObject(dialogue, centeredX, positionedY);
        
        System.out.println("DEBUG: Dialogue shown at (" + centeredX + ", " + positionedY + "): " + dialogue.getFullText());
        return true;
    }
    
    /**
     * Hide the current dialogue box
     */
    public void hideDialogue()
    {
        if (currentDialogue != null && currentWorld != null)
        {
            currentWorld.removeObject(currentDialogue);
            System.out.println("Dialogue hidden");
        }
        
        currentDialogue = null;
        currentWorld = null;
        currentNPC = null;
    }
    
    /**
     * Check if there's currently an active dialogue
     */
    public boolean isDialogueActive()
    {
        return currentDialogue != null && currentWorld != null;
    }
    
    /**
     * Get the current active dialogue
     */
    public DialogueBox getCurrentDialogue()
    {
        return currentDialogue;
    }
    
    /**
     * Get the NPC that triggered the current dialogue
     */
    public NPC getCurrentNPC()
    {
        return currentNPC;
    }
    
    /**
     * Process input for dialogue interaction
     * Should be called from the main world's act() method
     */
    public void processInput()
    {
        if (currentDialogue != null)
        {
            // Check for ENTER key to dismiss dialogue
            if (Greenfoot.isKeyDown("enter"))
            {
                hideDialogue();
            }
        }
    }
    
    /**
     * Reset the dialogue manager (useful when changing worlds)
     */
    public void reset()
    {
        if (currentDialogue != null && currentWorld != null)
        {
            currentWorld.removeObject(currentDialogue);
        }
        currentDialogue = null;
        currentWorld = null;
        currentNPC = null;
    }
}
