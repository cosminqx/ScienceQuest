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
        checkPlayerProximity();
        checkDialogueInteraction();
    }
    
    /**
     * Check if player presses F key nearby to initiate dialogue
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
            
            // Debug output
            boolean fKeyDown = Greenfoot.isKeyDown("f");
            if (distance < INTERACTION_DISTANCE)
            {
                System.out.println("DEBUG: Player in range. Distance: " + distance + ", F key: " + fKeyDown);
            }
            
            // Check if player is close enough and F key is pressed
            if (distance < INTERACTION_DISTANCE && fKeyDown)
            {
                // Prevent repeated dialogue triggers from holding F key
                if (!fKeyPressed)
                {
                    System.out.println("DEBUG: F key pressed, initiating dialogue");
                    fKeyPressed = true;
                    initiateDialogue();
                }
            }
            else if (!fKeyDown)
            {
                // Reset when F key is released
                fKeyPressed = false;
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
        
        // Create and show the dialogue
        String playerName = PlayerData.getPlayerName();
        String dialogueText = getDialogueText(playerName);
        String iconPath = getIconPath();
        
        // Greeting dialogue
        DialogueBox dialogue = new DialogueBox(dialogueText, iconPath, true);
        dialogue.setTypewriterSpeed(2);

        // Queue the quiz question to show after greeting
        DialogueQuestion question = buildScienceQuestion();
        DialogueBox questionBox = new DialogueBox(question, iconPath, true);
        questionBox.setTypewriterSpeed(2);
        manager.queueDialogue(questionBox);
        
        manager.showDialogue(dialogue, world, this);
    }

    private DialogueQuestion buildScienceQuestion()
    {
        String questionText = "Hai sa incepi cu o intrebare usoara de stiinta: Care este cea mai mica forma a universului?";
        String[] answers = { "Atom", "Molecula", "Celula", "Galaxie" };
        int correctIndex = 0;
        String correctResponse = "Corect! Tot in univers este construit din atomi. Bravo!";
        String incorrectResponse = "Nu chiar. Raspunsul corect este atomul. Vom acoperi asta mai detaliat curand.";
        return new DialogueQuestion(questionText, answers, correctIndex, correctResponse, incorrectResponse);
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
        return "Salut " + playerName + "!\n" +
            "---\n" +
            "Bine ai venit la Science Quests!\n" +
            "---\n" +
            "Simte-te liber sa explorezi si sa inveți.";
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
