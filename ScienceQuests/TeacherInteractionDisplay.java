import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * TeacherInteractionDisplay - Shows an image above the teacher when the player is nearby
 */
public class TeacherInteractionDisplay extends Actor
{
    public TeacherInteractionDisplay()
    {
        GreenfootImage image = new GreenfootImage(48, 48);
        image.setColor(new Color(0, 0, 0, 0));
        image.fillRect(0, 0, 48, 48);
        setImage(image);
    }

    public void act()
    {
        // Visual-only actor; visibility controlled by Teacher
    }
}
