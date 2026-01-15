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
    private static final int CROP_WIDTH = 60;
    private static final int CROP_HEIGHT = 80;

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
            
            // Crop to remove transparent areas - center the character
            int startX = (int)Math.round(targetW / 2 - CROP_WIDTH / 2);
            int startY = (int)Math.round(targetH / 2 - CROP_HEIGHT / 2);
            for (int i = 0; i < frames.length; i++)
            {
                GreenfootImage croppedFrame = new GreenfootImage(CROP_WIDTH, CROP_HEIGHT);
                croppedFrame.drawImage(frames[i], -startX, -startY);
                frames[i] = croppedFrame;
            }
            
            setImage(frames[0]);
        }
        catch (Exception e)
        {
            // Fallback: create a simple pink rectangle if spritesheet not found
            frames = new GreenfootImage[1];
            GreenfootImage image = new GreenfootImage(CROP_WIDTH, CROP_HEIGHT);
            image.setColor(new Color(255, 100, 200)); // Pink
            image.fillRect(0, 0, CROP_WIDTH, CROP_HEIGHT);
            image.setColor(Color.WHITE);
            image.fillOval(18, 12, 8, 8);
            image.fillOval(34, 12, 8, 8);
            image.drawLine(22, 32, 38, 32);
            frames[0] = image;
            setImage(frames[0]);
        }
    }

    public void act()
    {
        System.out.println("Girl act() called, position: " + getX() + ", " + getY());
        handleInput();
        
        counter++;
        if (counter >= delay)
        {
            frameIndex = (frameIndex + 1) % frames.length;
            setImage(frames[frameIndex]);
            counter = 0;
        }
    }

    private void handleInput()
    {
        if (Greenfoot.isKeyDown("up"))
        {
            setLocation(getX(), getY() - speed);
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                System.out.println("Collision detected moving up!");
                setLocation(getX(), getY() + speed);
            }
        }
        if (Greenfoot.isKeyDown("down"))
        {
            setLocation(getX(), getY() + speed);
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                System.out.println("Collision detected moving down!");
                setLocation(getX(), getY() - speed);
            }
        }
        if (Greenfoot.isKeyDown("left"))
        {
            setLocation(getX() - speed, getY());
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                System.out.println("Collision detected moving left!");
                setLocation(getX() + speed, getY());
            }
        }
        if (Greenfoot.isKeyDown("right"))
        {
            setLocation(getX() + speed, getY());
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                System.out.println("Collision detected moving right!");
                setLocation(getX() - speed, getY());
            }
        }
    }
}
