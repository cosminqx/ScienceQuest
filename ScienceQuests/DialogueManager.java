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
    private boolean enterKeyPressed = false; // Track ENTER key state to prevent repeated advances
    private DialogueBox queuedDialogue; // Dialogue to show after the current one
    private int inputCooldown = 0; // Small cooldown to avoid carrying key presses between dialogues
    private boolean questionReady = true; // Wait until ENTER is released before accepting question input
    private int questionReadyDelay = 0; // Additional frames to wait after release
    private boolean waitingForEnterRelease = false; // After question answer, wait for ENTER to be released before accepting input
    private boolean questionInputEnabled = false; // Only accept question input after ENTER is released at least once
    private int questionInputClearFrames = 0; // Additional frames to wait after enabling input to clear key buffer
    
    /**
     * Private constructor (singleton pattern)
     */
    private DialogueManager()
    {
        currentDialogue = null;
        currentWorld = null;
        currentNPC = null;
        enterKeyPressed = false;
        queuedDialogue = null;
        inputCooldown = 0;
        questionReady = true;
        questionReadyDelay = 0;
        waitingForEnterRelease = false;
        questionInputEnabled = false;
        questionInputClearFrames = 0;
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
        enterKeyPressed = false; // reset key state on new dialogue
        
        // For questions, use longer cooldown to prevent input carryover
        if (dialogue.isQuestionMode())
        {
            inputCooldown = 15; // Longer cooldown for questions to prevent immediate advance
        }
        else
        {
            inputCooldown = 6; // Standard cooldown for normal dialogue
        }
        
        questionReady = !dialogue.isQuestionMode(); // question input locked until released
        questionReadyDelay = dialogue.isQuestionMode() ? 10 : 0; // wait a few frames after release
        waitingForEnterRelease = false; // reset the enter release flag
        questionInputEnabled = false; // No question input until ENTER is released
        questionInputClearFrames = 0; // Clear any buffered keys
        
        // Position the dialogue box at the bottom center of the screen
        int worldWidth = world.getWidth();
        int centeredX = worldWidth / 2;
        int positionedY = world.getHeight() - 60; // 60 pixels from bottom
        
        world.addObject(dialogue, centeredX, positionedY);
        
        System.out.println("DEBUG: Dialogue shown at (" + centeredX + ", " + positionedY + "): " + dialogue.getFullText());
        return true;
    }

    /**
     * Queue a dialogue to be shown immediately after the current one finishes
     */
    public void queueDialogue(DialogueBox dialogue)
    {
        queuedDialogue = dialogue;
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
        enterKeyPressed = false;
        questionReady = true;
        questionReadyDelay = 0;
        waitingForEnterRelease = false;
        questionInputEnabled = false;
        questionInputClearFrames = 0;
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
            // Prevent immediate re-trigger right after showing dialogue
            if (inputCooldown > 0)
            {
                inputCooldown--;
                return;
            }

            // Handle question-mode dialogues separately
            if (currentDialogue.isQuestionMode())
            {
                boolean enterKeyDown = Greenfoot.isKeyDown("enter");
                
                // First, wait for ENTER to be released if it wasn't when question appeared
                if (!questionInputEnabled)
                {
                    if (!enterKeyDown)
                    {
                        // ENTER is now released, enable question input
                        questionInputEnabled = true;
                        questionInputClearFrames = 5; // Wait 5 more frames to clear key buffer
                        System.out.println("DEBUG: ENTER released - waiting 5 frames to clear buffer");
                    }
                    else
                    {
                        // Still holding ENTER, wait
                        System.out.println("DEBUG: Waiting for ENTER release...");
                    }
                    return; // Don't process input yet
                }
                
                // Wait for key buffer to clear after enabling input
                if (questionInputClearFrames > 0)
                {
                    questionInputClearFrames--;
                    System.out.println("DEBUG: Clearing key buffer... " + questionInputClearFrames + " frames remaining");
                    // Clear any keys from the buffer
                    Greenfoot.getKey(); // Consume and discard any buffered key
                    return; // Don't process input yet
                }
                
                // Now that input is enabled and buffer is clear, process question input
                String key = Greenfoot.getKey();
                if (key != null)
                {
                    System.out.println("DEBUG: Question key pressed: " + key);
                    if ("1".equals(key)) currentDialogue.selectIndex(0);
                    else if ("2".equals(key)) currentDialogue.selectIndex(1);
                    else if ("3".equals(key)) currentDialogue.selectIndex(2);
                    else if ("4".equals(key)) currentDialogue.selectIndex(3);
                    else if ("up".equals(key) || "w".equals(key)) currentDialogue.moveSelection(-1);
                    else if ("down".equals(key) || "s".equals(key)) currentDialogue.moveSelection(1);
                    else if ("enter".equals(key))
                    {
                        System.out.println("DEBUG: Confirming answer...");
                        // Confirm answer and show follow-up response
                        boolean correct = currentDialogue.confirmSelection();
                        DialogueQuestion q = currentDialogue.getQuestion();
                        String responseText = correct ? q.getCorrectResponse() : q.getIncorrectResponse();
                        String iconPath = currentDialogue.getIconPath();
                        World w = currentWorld;
                        NPC npc = currentNPC;
                        hideDialogue();
                        DialogueBox response = new DialogueBox(responseText, iconPath, true);
                        response.setTypewriterSpeed(2);
                        showDialogue(response, w, npc);
                        // Flag to wait for ENTER release before accepting input on response
                        waitingForEnterRelease = true;
                        return;
                    }
                }
                return; // Do not process normal ENTER while question active
            }
            
            // Check for ENTER key
            boolean enterKeyDown = Greenfoot.isKeyDown("enter");
            
            // Safety check: if somehow we're still in question mode, don't process normal input
            if (currentDialogue.isQuestionMode())
            {
                System.out.println("ERROR: Question mode handling fell through to normal input processing!");
                return;
            }
            
            // If we're waiting for ENTER to be released (after question confirmation)
            if (waitingForEnterRelease)
            {
                if (!enterKeyDown)
                {
                    // ENTER has been released, now accept input normally
                    waitingForEnterRelease = false;
                    enterKeyPressed = false;
                }
                return; // Don't process input while waiting for release
            }
            
            if (enterKeyDown && !enterKeyPressed)
            {
                // ENTER key just pressed (not held from previous frame)
                
                // Check if the dialogue is still animating
                if (!currentDialogue.isFullyDisplayed())
                {
                    // Animation running: Skip the typewriter animation
                    enterKeyPressed = true;
                    currentDialogue.skipTypewriter();
                    System.out.println("DEBUG: Skipped typewriter animation");
                }
                else
                {
                    // Animation finished: Advance to the next page
                    enterKeyPressed = true;
                    System.out.println("DEBUG: Animation complete, advancing page");
                    
                    if (!currentDialogue.nextPage())
                    {
                        DialogueBox next = queuedDialogue;
                        World w = currentWorld;
                        NPC npc = currentNPC;
                        queuedDialogue = null;
                        hideDialogue();
                        if (next != null)
                        {
                            System.out.println("DEBUG: Showing queued dialogue");
                            showDialogue(next, w, npc);
                        }
                        else
                        {
                            // No more pages, so dismiss the dialogue
                            System.out.println("DEBUG: Last page reached, dismissing dialogue");
                        }
                    }
                    else
                    {
                        System.out.println("DEBUG: Advanced dialogue page");
                    }
                }
            }
            else if (!enterKeyDown)
            {
                // Reset when ENTER key is released
                if (enterKeyPressed)
                {
                    System.out.println("DEBUG: ENTER released, resetting enterKeyPressed");
                }
                enterKeyPressed = false;
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
        enterKeyPressed = false;
        inputCooldown = 0;
        questionReady = true;
        questionReadyDelay = 0;
        waitingForEnterRelease = false;
        questionInputEnabled = false;
        questionInputClearFrames = 0;
    }
}
