import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Desk - A collision object that blocks player movement
 */
public class Desk extends Actor
{
    public Desk()
    {
        try
        {
            GreenfootImage image = new GreenfootImage("images/collision-desk.png");
            image.scale(60, 50);
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: create a simple brown rectangle if image not found
            GreenfootImage image = new GreenfootImage(60, 50);
            image.setColor(new Color(139, 69, 19)); // Brown
            image.fillRect(0, 0, 60, 50);
            setImage(image);
        }
    }

    /**
     * Act - do whatever the Desk wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        // Desk is a static collision object - no action needed
    }
}
