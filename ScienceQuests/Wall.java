import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Wall - A collision object that blocks player movement
 */
public class Wall extends Actor
{
    public Wall()
    {
        try
        {
            GreenfootImage image = new GreenfootImage("images/collision-wall.png");
            image.scale(50, 50);
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: create a simple gray rectangle if image not found
            GreenfootImage image = new GreenfootImage(50, 50);
            image.setColor(new Color(128, 128, 128)); // Gray
            image.fillRect(0, 0, 50, 50);
            setImage(image);
        }
    }

    /**
     * Act - do whatever the Wall wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        // Wall is a static collision object - no action needed
    }
}
