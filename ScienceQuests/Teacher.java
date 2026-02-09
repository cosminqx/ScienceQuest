import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Teacher - A static NPC character (man teacher) in the classroom
 * Implements NPC interface for dialogue system
 */
public class Teacher extends Actor implements NPC
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80; // Distance in pixels to trigger interaction
    private boolean fKeyPressed = false; // Track F key state to prevent repeated presses
    private int dialogueCooldown = 0;
    
    public Teacher()
    {
        try
        {
            // Invisible, small collision hitbox
            GreenfootImage image = new GreenfootImage(40, 60);
            image.setTransparency(0);
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: small visible gray box as collider
            GreenfootImage image = new GreenfootImage(40, 60);
            image.setColor(new Color(100, 100, 100)); // Gray
            image.fillRect(0, 0, 40, 60);
            setImage(image);
        }
        
        // Create interaction display (initially not in world)
        interactionDisplay = new TeacherInteractionDisplay();
    }

    /**
     * Act - do whatever the Teacher wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        if (dialogueCooldown > 0)
        {
            dialogueCooldown--;
        }
        checkPlayerProximity();
        checkDialogueInteraction();
    }
    
    /**
     * Check if player is nearby to initiate dialogue (auto-trigger when close)
     */
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null) return;
        
        // Find the player character (Boy or Girl)
        Actor player = null;
        if (!world.getObjects(Boy.class).isEmpty())
        {
            player = world.getObjects(Boy.class).get(0);
        }
        else if (!world.getObjects(Girl.class).isEmpty())
        {
            player = world.getObjects(Girl.class).get(0);
        }
        
        if (player != null)
        {
            // Calculate distance to player
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // Auto-trigger dialogue when player is nearby (no F key needed)
            if (distance < INTERACTION_DISTANCE)
            {
                // Stop showing dialogue after 5/5 quizzes complete
                GameState gameState = GameState.getInstance();
                if (!gameState.hasMainMapNPCQuizzesRemaining())
                {
                    return;
                }
                
                // Check if dialogue is not already active
                DialogueManager manager = DialogueManager.getInstance();
                if (manager.isDialogueActive())
                {
                    return;
                }

                if (fKeyPressed)
                {
                    // Dialogue closed - allow next question after short cooldown
                    fKeyPressed = false;
                    dialogueCooldown = 15;
                    return;
                }

                if (dialogueCooldown == 0)
                {
                    fKeyPressed = true;
                    initiateDialogue();
                }
            }
            else
            {
                // Reset when player moves away
                fKeyPressed = false;
                dialogueCooldown = 0;
            }
        }
    }
    
    /**
     * Initiate dialogue with the teacher
     */
    private void initiateDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        // Get the dialogue manager
        DialogueManager manager = DialogueManager.getInstance();
        
        // Check if dialogue is already active
        if (manager.isDialogueActive())
        {
            return;
        }
        
        GameState gameState = GameState.getInstance();
        
        // Check if MainMapWorld NPC quizzes are exhausted
        if (!gameState.hasMainMapNPCQuizzesRemaining())
        {
            // All 5 quizzes done - show completion dialogue
            String completionText = gameState.areMainMapQuestsUnlocked() 
                ? "Bravo! Ai răspuns corect la " + gameState.getMainMapNPCCorrect() + "/5 întrebări.\n---\nAcum mergi la Laboratorul de Biologie (săgeți de jos).\n---\nAcolo vei rezolva mai multe puzzle-uri!"
                : "Ai terminat toate 5 întrebări! Continuă cu celelalte activități pe hartă.";
            
            DialogueBox completion = new DialogueBox(completionText, getIconPath(), true);
            completion.setTypewriterSpeed(2);
            manager.showDialogue(completion, world, this);
            return;
        }
        
        // Create and show the dialogue
        String playerName = PlayerData.getPlayerName();
        String iconPath = getIconPath();
        
        int total = gameState.getMainMapNPCProgress();
        int correct = gameState.getMainMapNPCCorrect();

        // Show introduction only on first question
        if (total == 0)
        {
            String dialogueText = "Salut " + playerName + "! Sunt profesorul din clasă.\n" +
                "---\n" +
                "Iată o întrebare de știință pentru tine:\n" +
                "Quiz-ul 1 din 5 (0 corecte)\n" +
                "---\n" +
                "Răspunde corect la 3 din 5 întrebări pentru a debloca mini-quest-urile.";
            
            DialogueBox greeting = new DialogueBox(dialogueText, iconPath, true);
            greeting.setTypewriterSpeed(2);
            
            // Queue the quiz question to show after greeting
            DialogueQuestion question = buildScienceQuestion();
            DialogueBox questionBox = new DialogueBox(question, iconPath, true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordMainMapNPCQuizResult(isCorrect);
                int t = gs.getMainMapNPCProgress();
                int c = gs.getMainMapNPCCorrect();
                DebugLog.log("MainMap NPC Quiz Result: " + c + "/" + t + " correct");
                if (gs.areMainMapQuestsUnlocked())
                {
                    DebugLog.log("QUESTS UNLOCKED! Correct: " + c + "/5");
                }
            });
            
            manager.queueDialogue(questionBox);
            manager.showDialogue(greeting, world, this);
        }
        else
        {
            // Show quiz question directly for subsequent questions
            DialogueQuestion question = buildScienceQuestion();
            DialogueBox questionBox = new DialogueBox(question, iconPath, true);
            questionBox.setTypewriterSpeed(2);
            
            // Set callback to record attempt with correctness flag
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordMainMapNPCQuizResult(isCorrect);
                int t = gs.getMainMapNPCProgress();
                int c = gs.getMainMapNPCCorrect();
                DebugLog.log("MainMap NPC Quiz Result: " + c + "/" + t + " correct");
                if (gs.areMainMapQuestsUnlocked())
                {
                    DebugLog.log("QUESTS UNLOCKED! Correct: " + c + "/5");
                }
            });
            
            manager.showDialogue(questionBox, world, this);
        }
    }

    private DialogueQuestion buildScienceQuestion()
    {
        return GameState.getInstance().getRandomQuestion("general", QuestionPools.getGeneralScienceQuestions());
    }
    
    /**
     * Check if player is nearby and show/hide interaction display
     */
    private void checkPlayerProximity()
    {
        World world = getWorld();
        if (world == null) return;
        
        // Find the player character (Boy or Girl)
        Actor player = null;
        if (!world.getObjects(Boy.class).isEmpty())
        {
            player = world.getObjects(Boy.class).get(0);
        }
        else if (!world.getObjects(Girl.class).isEmpty())
        {
            player = world.getObjects(Girl.class).get(0);
        }
        
        if (player != null)
        {
            // Calculate distance to player
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // Show/hide interaction display based on distance
            if (distance < INTERACTION_DISTANCE)
            {
                // Player is close - show display above teacher
                if (interactionDisplay.getWorld() == null)
                {
                    world.addObject(interactionDisplay, getX(), getY() - 80);
                }
            }
            else
            {
                // Player is far - hide display
                if (interactionDisplay.getWorld() != null)
                {
                    world.removeObject(interactionDisplay);
                }
            }
        }
    }
    
    /**
     * Get the dialogue text for this NPC (from NPC interface)
     */
    @Override
    public String getDialogueText(String playerName)
    {
        GameState gameState = GameState.getInstance();
        int correct = gameState.getMainMapNPCCorrect();
        int total = gameState.getMainMapNPCProgress();
        boolean unlocked = gameState.areMainMapQuestsUnlocked();
        
        String progressText = "Quiz-ul " + (total + 1) + " din 5";
        String unlockStatus = unlocked 
            ? "✓ Mini-quest-urile sunt DEZBLOCATE!" 
            : "Răspunde corect la 3 din 5 întrebări pentru a debloca mini-quest-urile.";
        
        return "Salut " + playerName + "! Sunt profesorul din clasă.\n" +
            "---\n" +
            "Iată o întrebare de știință pentru tine:\n" +
            progressText + " (" + correct + " corecte)\n" +
            "---\n" +
            unlockStatus;
    }
    
    private String getDialogueText(String playerName, GameState gameState)
    {
        int correct = gameState.getMainMapNPCCorrect();
        int total = gameState.getMainMapNPCProgress();
        boolean unlocked = gameState.areMainMapQuestsUnlocked();
        
        String progressText = "Quiz-ul " + (total + 1) + " din 5";
        String unlockStatus = unlocked 
            ? "✓ Mini-quest-urile sunt DEZBLOCATE!" 
            : "Răspunde corect la 3 din 5 întrebări pentru a debloca mini-quest-urile.";
        
        return "Salut " + playerName + "! Sunt profesorul din clasă.\n" +
            "---\n" +
            "Iată o întrebare de știință pentru tine:\n" +
            progressText + " (" + correct + " corecte)\n" +
            "---\n" +
            unlockStatus;
    }
    
    /**
     * Get the icon path for this NPC's dialogue box (from NPC interface)
     */
    @Override
    public String getIconPath()
    {
        return "images/man_teacher_icon.png";
    }
}
