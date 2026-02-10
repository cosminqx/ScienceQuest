import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Teacher - A static NPC character (man teacher) in the classroom
 * Implements NPC interface for dialogue system
 */
public class Teacher extends QuizNPCBase
{
    private boolean completionShown = false;

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
        
    }

    /**
     * Act - do whatever the Teacher wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    protected boolean isInteractionEnabled()
    {
        return !completionShown;
    }

    protected void onInteract(World world)
    {
        initiateDialogue();
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
        
        // Create and show the dialogue
        String playerName = PlayerData.getPlayerName();
        String iconPath = getIconPath();
        
        int total = gameState.getMainMapNPCProgress();
        int correct = gameState.getMainMapNPCCorrect();

        if (total >= 5 && correct < 3)
        {
            String retryText = "Nu ai suficiente răspunsuri corecte.\n---\n" +
                "Mai ai nevoie de " + (3 - correct) + " corecte.";
            DialogueBox retry = new DialogueBox(retryText, iconPath, true);
            retry.setTypewriterSpeed(2);

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
            manager.showDialogue(retry, world, this);
            return;
        }

        if (total >= 5)
        {
            String completionText = gameState.areMainMapQuestsUnlocked()
                ? "Bravo! Ai răspuns corect la " + correct + "/5 întrebări.\n---\nAcum rezolvă și mini-jocurile notate cu '!' (apasă SPACE în jurul lor)."
                : "Ai terminat toate 5 întrebări! Continuă cu celelalte activități pe hartă.";

            DialogueBox completion = new DialogueBox(completionText, iconPath, true);
            completion.setTypewriterSpeed(2);
            manager.showDialogue(completion, world, this);
            completionShown = true;
            return;
        }

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
