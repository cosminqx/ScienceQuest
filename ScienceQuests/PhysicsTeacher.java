import greenfoot.*;

/**
 * PhysicsTeacher - Teacher in the physics lab who reacts when equipment breaks
 */
public class PhysicsTeacher extends QuizNPCBase
{
    private boolean hasShownPanicDialogue = false;
    private LabFizicaWorld labWorld;
    
    public PhysicsTeacher()
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
            image.setColor(new Color(100, 100, 100));
            image.fillRect(0, 0, 40, 60);
            setImage(image);
        }
        
    }

    protected void onWorldTick(World world)
    {
        if (labWorld == null && world instanceof LabFizicaWorld)
        {
            labWorld = (LabFizicaWorld) world;
        }
    }

    protected boolean isInteractionEnabled()
    {
        GameState state = GameState.getInstance();
        return labWorld != null && !state.isLabPhysQuizGateComplete();
    }

    protected boolean shouldShowPrompt()
    {
        return labWorld != null && labWorld.isBroken();
    }

    protected void onInteract(World world)
    {
        initiateRepairDialogue();
    }
    
    /**
     * Show panic reaction when equipment breaks
     */
    public void showPanicReaction()
    {
        if (hasShownPanicDialogue) return;
        hasShownPanicDialogue = true;
        
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        // Panic dialogue
        String panicText = "O NU! Echipamentul din laborator s-a stricat!\n---\nTrebuie să-l reparăm urgent!";
        DialogueBox panicDialogue = new DialogueBox(panicText, getIconPath(), true);
        panicDialogue.setTypewriterSpeed(2);
        
        manager.showDialogue(panicDialogue, world, this);
    }
    
    
    /**
     * Show physics lab quiz dialogue
     */
    private void initiateRepairDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        if (manager.isDialogueActive()) return;
        
        GameState state = GameState.getInstance();
        int total = state.getLabPhysQuizTotal();
        int correct = state.getLabPhysQuizCorrect();

        if (state.isLabPhysQuizGateComplete())
        {
            String doneText = "Ai terminat toate cele 5 întrebări.\n---\n" +
                "Corecte: " + correct + "/5.\n---\n" +
                "Continuă cu mini‑quest‑urile.";
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

            DialogueQuestion question = buildPhysicsQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabPhysNPCQuizResult(isCorrect);
            });

            manager.queueDialogue(questionBox);
            manager.showDialogue(retry, world, this);
            return;
        }

        // Show introduction only on first question
        if (total == 0)
        {
            String repairText = "Bine ai venit la Laboratorul de Fizică!\n---\n" +
                "Întrebarea 1 din 5.\n---\n" +
                "Corecte: 0/5.";
            DialogueBox instruction = new DialogueBox(repairText, getIconPath(), true);
            instruction.setTypewriterSpeed(2);
            
            // Physics question
            DialogueQuestion question = buildPhysicsQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabPhysNPCQuizResult(isCorrect);
            });
            
            manager.queueDialogue(questionBox);
            manager.showDialogue(instruction, world, this);
        }
        else
        {
            // Show question directly for subsequent questions
            DialogueQuestion question = buildPhysicsQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabPhysNPCQuizResult(isCorrect);
            });
            
            manager.showDialogue(questionBox, world, this);
        }
    }
    
    private DialogueQuestion buildPhysicsQuestion()
    {
        return GameState.getInstance().getRandomQuestion("physics", QuestionPools.getPhysicsQuestions());
    }
    
    
    @Override
    public String getDialogueText(String playerName)
    {
        return "Bună ziua! Eu sunt profesorul de fizică.";
    }
    
    @Override
    public String getIconPath()
    {
        return "images/man_teacher_icon.png";
    }
}
