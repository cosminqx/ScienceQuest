import greenfoot.*;

/**
 * PhysicsTeacher - Teacher in the physics lab who reacts when equipment breaks
 */
public class PhysicsTeacher extends Actor implements NPC
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
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
        
        interactionDisplay = new TeacherInteractionDisplay();
    }
    
    public void act()
    {
        // Store reference to world
        if (labWorld == null && getWorld() instanceof LabFizicaWorld)
        {
            labWorld = (LabFizicaWorld) getWorld();
        }
        
        checkPlayerProximity();
        checkDialogueInteraction();
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
            
            if (distance < INTERACTION_DISTANCE)
            {
                DialogueManager manager = DialogueManager.getInstance();
                if (!manager.isDialogueActive() && !fKeyPressed)
                {
                    fKeyPressed = true;
                    initiateRepairDialogue();
                }
            }
            else
            {
                fKeyPressed = false;
            }
        }
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

        if (total >= 5)
        {
            String doneText = "Ai terminat toate cele 5 întrebări.\n---\n" +
                "Corecte: " + correct + "/5.\n---\n" +
                "Continuă cu mini‑quest‑urile.";
            DialogueBox done = new DialogueBox(doneText, getIconPath(), true);
            done.setTypewriterSpeed(2);
            manager.showDialogue(done, world, this);
            return;
        }

        String repairText = "Întrebarea " + (total + 1) + " din 5.\n---\n" +
            "Corecte: " + correct + "/5.";
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
    
    private DialogueQuestion buildPhysicsQuestion()
    {
        return GameState.getInstance().getRandomQuestion("physics", QuestionPools.getPhysicsQuestions());
    }
    
    /**
     * Check player proximity for interaction prompt
     */
    private void checkPlayerProximity()
    {
        World world = getWorld();
        if (world == null || labWorld == null) return;
        
        // Only show interaction when lab is broken
        if (!labWorld.isBroken())
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
                if (interactionDisplay.getWorld() == null)
                {
                    world.addObject(interactionDisplay, getX(), getY() - 80);
                }
            }
            else
            {
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
        
        if (!world.getObjects(Boy.class).isEmpty())
        {
            return world.getObjects(Boy.class).get(0);
        }
        else if (!world.getObjects(Girl.class).isEmpty())
        {
            return world.getObjects(Girl.class).get(0);
        }
        return null;
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
