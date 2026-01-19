/**
 * EXAMPLE: How to create different NPC characters with dialogue
 * This file shows patterns you can use when implementing dialogue for new NPCs
 */

// ============================================================================
// EXAMPLE 1: Simple NPC with Basic Dialogue
// ============================================================================

/*
public class Librarian extends Actor implements NPC
{
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
    
    public Librarian()
    {
        GreenfootImage image = new GreenfootImage(40, 60);
        image.setColor(new Color(100, 100, 100));
        image.fillRect(0, 0, 40, 60);
        setImage(image);
    }
    
    public void act()
    {
        checkDialogueInteraction();
    }
    
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null) return;
        
        Actor player = findPlayer(world);
        if (player != null)
        {
            double distance = getDistance(player);
            
            if (distance < INTERACTION_DISTANCE && Greenfoot.isKeyDown("f"))
            {
                if (!fKeyPressed)
                {
                    fKeyPressed = true;
                    initiateDialogue(world);
                }
            }
            else if (!Greenfoot.isKeyDown("f"))
            {
                fKeyPressed = false;
            }
        }
    }
    
    private void initiateDialogue(World world)
    {
        DialogueManager manager = DialogueManager.getInstance();
        if (!manager.isDialogueActive())
        {
            String playerName = PlayerData.getPlayerName();
            DialogueBox dialogue = new DialogueBox(
                getDialogueText(playerName),
                getIconPath()
            );
            manager.showDialogue(dialogue, world, this);
        }
    }
    
    @Override
    public String getDialogueText(String playerName)
    {
        return "Welcome to the library, " + playerName + "!";
    }
    
    @Override
    public String getIconPath()
    {
        return "images/librarian_icon.png";
    }
    
    private Actor findPlayer(World world)
    {
        if (!world.getObjects(Boy.class).isEmpty())
            return world.getObjects(Boy.class).get(0);
        if (!world.getObjects(Girl.class).isEmpty())
            return world.getObjects(Girl.class).get(0);
        return null;
    }
    
    private double getDistance(Actor other)
    {
        int dx = other.getX() - getX();
        int dy = other.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
*/

// ============================================================================
// EXAMPLE 2: NPC with Context-Based Dialogue
// ============================================================================

/*
public class Coach extends Actor implements NPC
{
    private static final int INTERACTION_DISTANCE = 100;
    private boolean fKeyPressed = false;
    private boolean hasSpokenBefore = false; // Track if already talked to
    
    @Override
    public String getDialogueText(String playerName)
    {
        if (!hasSpokenBefore)
        {
            hasSpokenBefore = true;
            return "Hey " + playerName + "! Ready for PE class?";
        }
        else
        {
            return "Great effort today!";
        }
    }
    
    @Override
    public String getIconPath()
    {
        return "images/coach_icon.png";
    }
    
    // ... rest of implementation similar to Librarian ...
}
*/

// ============================================================================
// EXAMPLE 3: NPC with Multi-Line Dialogue (Typewriter Effect)
// ============================================================================

/*
public class Principal extends Actor implements NPC
{
    @Override
    public String getDialogueText(String playerName)
    {
        return "Good morning, " + playerName + "!\n" +
               "I hope you're having a great day.\n" +
               "Remember to be respectful to all.";
    }
    
    @Override
    public String getIconPath()
    {
        return "images/principal_icon.png";
    }
    
    private void initiateDialogue(World world)
    {
        DialogueManager manager = DialogueManager.getInstance();
        if (!manager.isDialogueActive())
        {
            String playerName = PlayerData.getPlayerName();
            DialogueBox dialogue = new DialogueBox(
                getDialogueText(playerName),
                getIconPath(),
                true  // Enable typewriter effect
            );
            dialogue.setTypewriterSpeed(2); // Adjust speed
            manager.showDialogue(dialogue, world, this);
        }
    }
    
    // ... rest of implementation ...
}
*/

// ============================================================================
// EXAMPLE 4: NPC with Gender-Based Dialogue
// ============================================================================

/*
public class StudentHelper extends Actor implements NPC
{
    @Override
    public String getDialogueText(String playerName)
    {
        String gender = PlayerData.getPlayerGender();
        
        if ("Male".equals(gender))
        {
            return "Hey buddy " + playerName + "!";
        }
        else if ("Female".equals(gender))
        {
            return "Hi there " + playerName + "!";
        }
        else
        {
            return "Hello, " + playerName + "!";
        }
    }
    
    @Override
    public String getIconPath()
    {
        return "images/student_icon.png";
    }
    
    // ... rest of implementation ...
}
*/

// ============================================================================
// EXAMPLE 5: NPC with Conditional Dialogue (Hidden Requirements)
// ============================================================================

/*
public class Science extends Actor implements NPC
{
    @Override
    public String getDialogueText(String playerName)
    {
        // This could check any condition you want
        int hoursStudied = GameState.getHoursStudied(); // Example
        
        if (hoursStudied < 5)
        {
            return "You should study more, " + playerName + ".";
        }
        else
        {
            return "Great work studying, " + playerName + "!";
        }
    }
    
    @Override
    public String getIconPath()
    {
        return "images/science_teacher.png";
    }
    
    // ... rest of implementation ...
}
*/

// ============================================================================
// HELPFUL UTILITY CLASS (Optional - for reducing code duplication)
// ============================================================================

/*
public abstract class BaseNPC extends Actor implements NPC
{
    protected static final int INTERACTION_DISTANCE = 80;
    protected boolean fKeyPressed = false;
    
    public final void act()
    {
        checkDialogueInteraction();
        actImpl(); // Child classes override this
    }
    
    protected void actImpl()
    {
        // Override in child classes for custom behavior
    }
    
    protected final void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null) return;
        
        Actor player = findPlayer(world);
        if (player == null) return;
        
        double distance = getDistance(player);
        
        if (distance < INTERACTION_DISTANCE && Greenfoot.isKeyDown("f"))
        {
            if (!fKeyPressed)
            {
                fKeyPressed = true;
                initiateDialogue(world);
            }
        }
        else if (!Greenfoot.isKeyDown("f"))
        {
            fKeyPressed = false;
        }
    }
    
    private void initiateDialogue(World world)
    {
        DialogueManager manager = DialogueManager.getInstance();
        if (!manager.isDialogueActive())
        {
            String playerName = PlayerData.getPlayerName();
            DialogueBox dialogue = createDialogue(playerName);
            manager.showDialogue(dialogue, world, this);
        }
    }
    
    protected DialogueBox createDialogue(String playerName)
    {
        return new DialogueBox(getDialogueText(playerName), getIconPath());
    }
    
    protected Actor findPlayer(World world)
    {
        if (!world.getObjects(Boy.class).isEmpty())
            return world.getObjects(Boy.class).get(0);
        if (!world.getObjects(Girl.class).isEmpty())
            return world.getObjects(Girl.class).get(0);
        return null;
    }
    
    protected double getDistance(Actor other)
    {
        int dx = other.getX() - getX();
        int dy = other.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}

// Then usage would be simpler:
public class CoolTeacher extends BaseNPC
{
    @Override
    public String getDialogueText(String playerName)
    {
        return "Yo, " + playerName + "!";
    }
    
    @Override
    public String getIconPath()
    {
        return "images/cool_teacher.png";
    }
    
    @Override
    protected void actImpl()
    {
        // Custom behavior for this NPC
    }
}
*/
