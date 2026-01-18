import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * TeacherDisplay - Visual-only sprite for the teacher; collision handled by Teacher collider
 */
public class TeacherDisplay extends Actor
{
    public TeacherDisplay()
    {
        try
        {
            GreenfootImage image = new GreenfootImage("images/man_teacher.png");
            image.scale(110, 100); // full visible size
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: visible gray rectangle
            GreenfootImage image = new GreenfootImage(110, 100);
            image.setColor(new Color(100, 100, 100));
            image.fillRect(0, 0, 110, 100);
            setImage(image);
        }
    }

    public void act()
    {
        // Visual-only actor; positioning handled externally
    }
}
