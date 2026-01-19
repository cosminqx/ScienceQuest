import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Teacher - A static NPC character (man teacher) in the classroom
 */
public class Teacher extends Actor
{
    private TeacherInteractionDisplay interactionDisplay;
    private static final int INTERACTION_DISTANCE = 80; // Distance in pixels to trigger interaction
    
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
}
