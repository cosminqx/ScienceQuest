import greenfoot.*;

/**
 * BiologyAssistant - Assistant in the biology lab who can also help restore it
 */
public class BiologyAssistant extends Actor implements NPC
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
    private LabBiologyWorld labWorld;
    
    public BiologyAssistant()
    {
        try
        {
            // Load man_teacher.png image
            GreenfootImage image = new GreenfootImage("images/man_teacher.png");
            // Scale it slightly smaller to differentiate
            image.scale((int)(image.getWidth() * 0.9), (int)(image.getHeight() * 0.9));
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: visible box with different color
            GreenfootImage image = new GreenfootImage(40, 60);
            image.setColor(new Color(100, 150, 150));
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
     * Show lab quiz dialogue (no repair on single question)
     */
    private void initiateRepairDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        if (manager.isDialogueActive()) return;
        
        GameState state = GameState.getInstance();
        int total = state.getLabBioQuizTotal();
        int correct = state.getLabBioQuizCorrect();

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
        
        // Biology question
        DialogueQuestion question = buildBiologyQuestion();
        DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
        questionBox.setTypewriterSpeed(2);
        
        questionBox.setOnAnswerAttemptCallback(isCorrect -> {
            GameState gs = GameState.getInstance();
            gs.recordLabBioNPCQuizResult(isCorrect);
        });
        
        manager.queueDialogue(questionBox);
        manager.showDialogue(instruction, world, this);
    }
    
    private DialogueQuestion buildBiologyQuestion()
    {
        return GameState.getInstance().getRandomQuestion("biology", QuestionPools.getBiologyQuestions());
    }
    
    /**
     * Check player proximity for interaction prompt
     */
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
        return "Salut, " + playerName + "! Sunt asistentul de laborator la biologie.\n---\nÎmi place să ajut la experimente și să mențin laboratorul în ordine!";
    }
}
