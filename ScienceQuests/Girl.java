import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Girl - Playable character that can be controlled with arrow keys
 */
public class Girl extends Actor
{
    private GreenfootImage[] frames;
    private int frameIndex = 0;
    private int delay = 5;
    private int counter = 0;
    private int speed = 3;

    public Girl()
    {
        int frameWidth = 480;
        int frameHeight = 320;
        double scale = 0.7;
        
        try
        {
            GreenfootImage sheet = new GreenfootImage("spritesheet/redgirlhair.png");
            frames = new GreenfootImage[9];
            
            for (int i = 0; i < 9; i++)
            {
                GreenfootImage frame = new GreenfootImage(frameWidth, frameHeight);
                frame.drawImage(sheet, -i * frameWidth, 0);
                frames[i] = frame;
            }
            
            // Scale all frames
            int targetW = (int)Math.round(frameWidth * scale);
            int targetH = (int)Math.round(frameHeight * scale);
            for (int i = 0; i < frames.length; i++)
            {
                frames[i].scale(targetW, targetH);
            }
            
            setImage(frames[0]);
        }
        catch (Exception e)
        {
            // Fallback: create a simple pink rectangle if spritesheet not found
            frames = new GreenfootImage[1];
            GreenfootImage image = new GreenfootImage(30, 40);
            image.setColor(new Color(255, 100, 200)); // Pink
            image.fillRect(0, 0, 30, 40);
            image.setColor(Color.WHITE);
            image.fillOval(8, 8, 5, 5);
            image.fillOval(17, 8, 5, 5);
            image.drawLine(10, 20, 20, 20);
            frames[0] = image;
            setImage(frames[0]);
        }
    }

    public void act()
    {
        counter++;
        if (counter >= delay)
        {
            frameIndex = (frameIndex + 1) % frames.length;
            setImage(frames[frameIndex]);
            counter = 0;
        }
        System.out.println("Girl act() called, position: " + getX() + ", " + getY());
        handleInput();
    }

    private void handleInput()
    {
        if (Greenfoot.isKeyDown("up"))
        {
            setLocation(getX(), getY() - speed);
            if (isTouching(Desk.class))
            {
                System.out.println("Collision detected moving up!");
                setLocation(getX(), getY() + speed);
            }
        }
        if (Greenfoot.isKeyDown("down"))
        {
            setLocation(getX(), getY() + speed);
            if (isTouching(Desk.class))
            {
                System.out.println("Collision detected moving down!");
                setLocation(getX(), getY() - speed);
            }
        }
        if (Greenfoot.isKeyDown("left"))
        {
            setLocation(getX() - speed, getY());
            if (isTouching(Desk.class))
            {
                System.out.println("Collision detected moving left!");
                setLocation(getX() + speed, getY());
            }
        }
        if (Greenfoot.isKeyDown("right"))
        {
            setLocation(getX() + speed, getY());
            if (isTouching(Desk.class))
            {
                System.out.println("Collision detected moving right!");
                setLocation(getX() - speed, getY());
            }
        }
    }
}
