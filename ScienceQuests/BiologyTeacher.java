import greenfoot.*;

/**
 * BiologyTeacher - Teacher in the biology lab who reacts when lab is destroyed
 * Also gives initial NPC quizzes before mini-quests
 */
public class BiologyTeacher extends Actor implements NPC
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
    private boolean hasShownPanicDialogue = false;
    private LabBiologyWorld labWorld;
    private static final int NPC_QUIZ_LIMIT_LAB = 3; // Number of quizzes in lab
    
    public BiologyTeacher()
    {
        try
        {
            // Load man_teacher.png image
            GreenfootImage image = new GreenfootImage("images/man_teacher.png");
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: visible box
            GreenfootImage image = new GreenfootImage(40, 60);
            image.setColor(new Color(150, 100, 150));
            image.fillRect(0, 0, 40, 60);
            setImage(image);
        }
        
        interactionDisplay = new TeacherInteractionDisplay();
    }
    
    public void act()
    {
        // Store reference to world
        if (labWorld == null && getWorld() instanceof LabBiologyWorld)
        {
            labWorld = (LabBiologyWorld) getWorld();
        }
        
        checkPlayerProximity();
        checkDialogueInteraction();
    }
    
    /**
     * Show panic reaction when lab is destroyed
     */
    public void showPanicReaction()
    {
        if (hasShownPanicDialogue) return;
        hasShownPanicDialogue = true;
        
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        // Panic dialogue
        String panicText = "O NU! Incendiul a distrus laboratorul de biologie!\n---\nTrebuie să-l restaurăm urgent!";
        DialogueBox panicDialogue = new DialogueBox(panicText, getIconPath(), true);
        panicDialogue.setTypewriterSpeed(2);
        
        manager.showDialogue(panicDialogue, world, this);
    }
    
    /**
     * Check for interaction (auto-trigger when nearby)
     */
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null || labWorld == null) return;
        
        Actor player = getPlayer();
        if (player != null)
        {
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // Auto-trigger dialogue when player is nearby (no F key needed)
            if (distance < INTERACTION_DISTANCE)
            {
                DialogueManager manager = DialogueManager.getInstance();
                if (!manager.isDialogueActive() && !fKeyPressed)
                {
                    fKeyPressed = true;
                    // Always use quiz dialogue; lab repair is handled by LabBiologyWorld
                    initiateNPCDialogue();
                }
            }
            else
            {
                // Reset when player moves away
                fKeyPressed = false;
            }
        }
    }
    
    /**
     * Show initial NPC quiz dialogue (before lab is destroyed)
     */
    private void initiateNPCDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        if (manager.isDialogueActive()) return;
        
        GameState gameState = GameState.getInstance();
        int total = gameState.getLabBioQuizTotal();
        int correct = gameState.getLabBioQuizCorrect();

        // If quiz limit reached, show completion message
        if (total >= 5)
        {
            String doneText = "Ai răspuns la toate cele 5 întrebări!\n---\n" +
                "Ai " + correct + "/5 corecte.\n---\n" +
                "Continuă cu mini‑quest‑urile din laborator.";
            DialogueBox done = new DialogueBox(doneText, getIconPath(), true);
            done.setTypewriterSpeed(2);
            manager.showDialogue(done, world, this);
            return;
        }

        // Greeting dialogue
        String playerName = PlayerData.getPlayerName();
        String greetText = "Salut " + playerName + "! Sunt profesorul de biologie.\n---\n" +
            "Întrebarea " + (total + 1) + " din 5.\n" +
            "Corecte: " + correct + "/5";
        
        DialogueBox greeting = new DialogueBox(greetText, getIconPath(), true);
        greeting.setTypewriterSpeed(2);
        
        // Biology question
        DialogueQuestion question = buildBiologyQuestion();
        DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
        questionBox.setTypewriterSpeed(2);
        
        // Set callback to track quiz result (for lab-specific tracking, not MainMap)
        questionBox.setOnAnswerAttemptCallback(isCorrect -> {
            gameState.recordLabBioNPCQuizResult(isCorrect);
            DebugLog.log("Lab Biology NPC Quiz: " + gameState.getMainMapNPCCorrect() + " correct in MainMap");
        });
        
        manager.queueDialogue(questionBox);
        manager.showDialogue(greeting, world, this);
    }
    
    private DialogueQuestion buildBiologyQuestion()
    {
        return GameState.getInstance().getRandomQuestion("biology", QuestionPools.getBiologyQuestions());
    }
    
    /**
     * Show repair question dialogue
     */
    private void initiateRepairDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        if (manager.isDialogueActive()) return;
        
        // Repair instruction dialogue
        String repairText = "Pentru a restaura laboratorul, trebuie să răspunzi corect la o întrebare de biologie!";
        DialogueBox instruction = new DialogueBox(repairText, getIconPath(), true);
        instruction.setTypewriterSpeed(2);
        
        // Biology question
        DialogueQuestion question = buildBiologyQuestion();
        DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
        questionBox.setTypewriterSpeed(2);
        
        // Add callback to fix lab when answered correctly
        questionBox.setOnCorrectAnswerCallback(() -> {
            if (labWorld != null)
            {
                labWorld.repairLab();
                
                // Mark biology lab as completed and award badge
                GameState state = GameState.getInstance();
                state.completeLab(LabType.BIOLOGY);
                state.awardBadge("biology_master");
                state.addXp(25); // XP for lab repair
                state.addXp(50); // Bonus XP for completing the lab
            }
        });
        
        manager.queueDialogue(questionBox);
        manager.showDialogue(instruction, world, this);
    }
    private void checkPlayerProximity()
    {
        World world = getWorld();
        if (world == null || labWorld == null) return;
        
        // Only show interaction prompt if lab is destroyed
        if (!labWorld.isDestroyed())
        {
            if (interactionDisplay.getWorld() != null)
            {
                world.removeObject(interactionDisplay);
            }
            return;
        }
        
        Actor player = getPlayer();
        if (player != null)
        {
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INTERACTION_DISTANCE)
            {
                // Show interaction prompt
                if (interactionDisplay.getWorld() == null)
                {
                    world.addObject(interactionDisplay, getX(), getY() - 40);
                }
                else
                {
                    interactionDisplay.setLocation(getX(), getY() - 40);
                }
            }
            else
            {
                // Hide interaction prompt
                if (interactionDisplay.getWorld() != null)
                {
                    world.removeObject(interactionDisplay);
                }
            }
        }
    }
    
    private Actor getPlayer()
    {
        World world = getWorld();
        if (world == null) return null;
        
        java.util.List<Boy> boys = world.getObjects(Boy.class);
        if (!boys.isEmpty()) return boys.get(0);
        
        java.util.List<Girl> girls = world.getObjects(Girl.class);
        if (!girls.isEmpty()) return girls.get(0);
        
        return null;
    }
    
    // NPC interface methods
    public String getIconPath()
    {
        return "images/man_teacher.png";
    }
    
    public String getDialogueText(String playerName)
    {
        return "Bună, " + playerName + "! Sunt profesoara de biologie.\n---\nStudiez științele naturii și lumea vie. Vrei să afli ceva despre biologie?";
    }
}
