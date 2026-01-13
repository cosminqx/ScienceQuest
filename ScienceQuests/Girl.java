import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Girl - Playable character that can be controlled with arrow keys
 */
public class Girl extends Actor
{
    private int speed = 3;

    public Girl()
    {
        // Load the girl actor image
        try
        {
            GreenfootImage image = new GreenfootImage("avatarfata.png");
            image.scale(50, 60);
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback: create a simple pink rectangle if image not found
            GreenfootImage image = new GreenfootImage(30, 40);
            image.setColor(new Color(255, 100, 200)); // Pink
            image.fillRect(0, 0, 30, 40);
            image.setColor(Color.WHITE);
            image.fillOval(8, 8, 5, 5);
            image.fillOval(17, 8, 5, 5);
            image.drawLine(10, 20, 20, 20);
            setImage(image);
        }
    }

    /**
     * Act - Handle keyboard input for movement
     */
    public void act()
    {
        handleMovement();
    }

    private void handleMovement()
    {
        // Handle arrow keys for movement
        if (Greenfoot.isKeyDown("up"))
        {
            setLocation(getX(), getY() - speed);
        }
        if (Greenfoot.isKeyDown("down"))
        {
            setLocation(getX(), getY() + speed);
        }
        if (Greenfoot.isKeyDown("left"))
        {
            setLocation(getX() - speed, getY());
        }
        if (Greenfoot.isKeyDown("right"))
        {
            setLocation(getX() + speed, getY());
        }
    }
}
