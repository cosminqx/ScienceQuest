import greenfoot.*;

/**
 * BiologyTeacher - Teacher in the biology lab who reacts when lab is destroyed
 * Also gives initial NPC quizzes before mini-quests
 */
public class BiologyTeacher extends QuizNPCBase
{
    private boolean hasShownPanicDialogue = false;
    private LabBiologyWorld labWorld;
    
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
        
    }

    protected void onWorldTick(World world)
    {
        if (labWorld == null && world instanceof LabBiologyWorld)
        {
            labWorld = (LabBiologyWorld) world;
        }
    }

    protected boolean isInteractionEnabled()
    {
        GameState gameState = GameState.getInstance();
        return labWorld != null && !gameState.isLabBioQuizGateComplete();
    }

    protected boolean shouldShowPrompt()
    {
        return labWorld != null && labWorld.isDestroyed();
    }

    protected void onInteract(World world)
    {
        initiateNPCDialogue();
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

        if (gameState.isLabBioQuizGateComplete())
        {
            String doneText = "Ai răspuns la toate cele 5 întrebări!\n---\n" +
                "Ai " + correct + "/5 corecte.\n---\n" +
                "Continuă cu mini‑quest‑urile din laborator.";
            DialogueBox done = new DialogueBox(doneText, getIconPath(), true);
            done.setTypewriterSpeed(2);
            manager.showDialogue(done, world, this);
            return;
        }

        if (total >= 5 && correct < 3)
        {
            String retryText = "Nu ai suficiente răspunsuri corecte.\n---\n" +
                "Mai ai nevoie de " + (3 - correct) + " corecte.";
            DialogueBox retry = new DialogueBox(retryText, getIconPath(), true);
            retry.setTypewriterSpeed(2);

            DialogueQuestion question = buildBiologyQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                gameState.recordLabBioNPCQuizResult(isCorrect);
            });

            manager.queueDialogue(questionBox);
            manager.showDialogue(retry, world, this);
            return;
        }

        // Show introduction only on first question
        if (total == 0)
        {
            String playerName = PlayerData.getPlayerName();
            String greetText = "Salut " + playerName + "! Sunt profesorul de biologie.\n---\n" +
                "Întrebarea 1 din 5.\n" +
                "Corecte: 0/5";
            
            DialogueBox greeting = new DialogueBox(greetText, getIconPath(), true);
            greeting.setTypewriterSpeed(2);
            
            // Biology question
            DialogueQuestion question = buildBiologyQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                gameState.recordLabBioNPCQuizResult(isCorrect);
            });
            
            manager.queueDialogue(questionBox);
            manager.showDialogue(greeting, world, this);
        }
        else
        {
            // Show question directly for subsequent questions
            DialogueQuestion question = buildBiologyQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            // Set callback to track quiz result (for lab-specific tracking, not MainMap)
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                gameState.recordLabBioNPCQuizResult(isCorrect);
            });
            
            manager.showDialogue(questionBox, world, this);
        }
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
