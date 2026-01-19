import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Teacher - A static NPC character (man teacher) in the classroom
 */
public class Teacher extends Actor
{
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
    }

    /**
     * Act - do whatever the Teacher wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    public void act()
    {
        // Teacher is a static NPC - no action needed
    }
}
