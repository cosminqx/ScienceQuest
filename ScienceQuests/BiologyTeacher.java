import greenfoot.*;

/**
 * BiologyTeacher - Teacher in the biology lab who reacts when lab is destroyed
 */
public class BiologyTeacher extends Actor implements NPC
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
    private boolean hasShownPanicDialogue = false;
    private LabBiologyWorld labWorld;
    
    public BiologyTeacher()
    {
        try
        {
            // Load woman_teacher.png image
            GreenfootImage image = new GreenfootImage("images/woman_teacher.png");
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
     * Check for F key interaction to restore the lab
     */
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null || labWorld == null) return;
        
        // Only allow interaction if lab is destroyed
        if (!labWorld.isDestroyed()) return;
        
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
            }
        });
        
        manager.queueDialogue(questionBox);
        manager.showDialogue(instruction, world, this);
    }
    
    private DialogueQuestion buildBiologyQuestion()
    {
        String questionText = "Care este procesul prin care plantele produc hrană folosind lumina soarelui?";
        String[] answers = { "Fotosinteza", "Respirația", "Fermentația", "Digestia" };
        int correctIndex = 0;
        String correctResponse = "Corect! Fotosinteza este procesul vital pentru plante. Laboratorul este restaurat!";
        String incorrectResponse = "Greșit. Răspunsul corect este Fotosinteza. Mai încearcă!";
        return new DialogueQuestion(questionText, answers, correctIndex, correctResponse, incorrectResponse);
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
        return "images/woman_teacher.png";
    }
    
    public String getDialogueText(String playerName)
    {
        return "Bună, " + playerName + "! Sunt profesoara de biologie.\n---\nStudiez științele naturii și lumea vie. Vrei să afli ceva despre biologie?";
    }
}
