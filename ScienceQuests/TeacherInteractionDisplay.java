import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * TeacherInteractionDisplay - Shows an image above the teacher when the player is nearby
 */
public class TeacherInteractionDisplay extends Actor
{
    public TeacherInteractionDisplay()
    {
        try
        {
            GreenfootImage image = new GreenfootImage("images/ccfdbf90-ca2e-472f-9ef7-fc7c4c415e8b_removalai_preview.png");
            // Scale down the image to 1/4 size
            int width = image.getWidth() / 4;
            int height = image.getHeight() / 4;
            image.scale(width, height);
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: visible yellow rectangle
            GreenfootImage image = new GreenfootImage(50, 50);
            image.setColor(new Color(255, 255, 0, 180)); // Semi-transparent yellow
            image.fillRect(0, 0, 50, 50);
            setImage(image);
        }
    }

    public void act()
    {
        // Visual-only actor; visibility controlled by Teacher
    }
}
