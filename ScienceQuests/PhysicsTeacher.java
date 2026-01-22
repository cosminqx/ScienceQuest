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
     * Check for F key interaction to fix the lab
     */
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null || labWorld == null) return;
        
        // Only allow interaction if lab is broken
        if (!labWorld.isBroken()) return;
        
        Actor player = getPlayer();
        if (player != null)
        {
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INTERACTION_DISTANCE && Greenfoot.isKeyDown("f"))
            {
                if (!fKeyPressed)
                {
                    fKeyPressed = true;
                    initiateRepairDialogue();
                }
            }
            else if (!Greenfoot.isKeyDown("f"))
            {
                fKeyPressed = false;
            }
        }
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
        String repairText = "Pentru a repara echipamentul, trebuie să răspunzi corect la o întrebare!";
        DialogueBox instruction = new DialogueBox(repairText, getIconPath(), true);
        instruction.setTypewriterSpeed(2);
        
        // Physics question
        DialogueQuestion question = buildPhysicsQuestion();
        DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
        questionBox.setTypewriterSpeed(2);
        
        // Add callback to fix lab when answered correctly
        questionBox.setOnCorrectAnswerCallback(() -> {
            if (labWorld != null)
            {
                labWorld.repairLab();
            }
        });
        
        manager.queueDialogue(questionBox);
        manager.showDialogue(instruction, world, this);
    }
    
    private DialogueQuestion buildPhysicsQuestion()
    {
        String questionText = "Care este unitatea de măsură pentru forță în sistemul internațional?";
        String[] answers = { "Newton", "Joule", "Watt", "Pascal" };
        int correctIndex = 0;
        String correctResponse = "Corect! Newton este unitatea pentru forță. Echipamentul este reparat!";
        String incorrectResponse = "Greșit. Răspunsul corect este Newton (N). Mai încearcă!";
        return new DialogueQuestion(questionText, answers, correctIndex, correctResponse, incorrectResponse);
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
