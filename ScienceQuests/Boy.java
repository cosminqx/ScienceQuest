import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Boy - Playable character that can be controlled with arrow keys
 * Uses directional spritesheets (UP, DOWN, LEFT, RIGHT) with 8 frames each
 */
public class Boy extends Actor
{
    private int speed = 3;
    
    // Directional spritesheets
    private GreenfootImage[] upFrames;
    private GreenfootImage[] downFrames;
    private GreenfootImage[] leftFrames;
    private GreenfootImage[] rightFrames;
    
    // Animation state
    private GreenfootImage[] currentAnimation;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private static final int FRAME_DELAY = 5; // acts between frames
    private boolean isMoving = false;
    private int lastDirection = 2; // 0=up, 1=left, 2=down, 3=right
    
    // Frame dimensions
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 64;
    private static final int DISPLAY_WIDTH = 288;  // 3x scale
    private static final int DISPLAY_HEIGHT = 192; // 3x scale
    private static final int FRAMES_PER_DIRECTION = 8;

    public Boy()
    {
        try
        {
            // Load all directional spritesheets
            upFrames = loadDirectionalFrames("spritesheet/boy/UP.png");
            downFrames = loadDirectionalFrames("spritesheet/boy/DOWN.png");
            leftFrames = loadDirectionalFrames("spritesheet/boy/LEFT.png");
            rightFrames = loadDirectionalFrames("spritesheet/boy/RIGHT.png");
            
            // Start with down-facing idle frame
            currentAnimation = downFrames;
            currentFrame = 0;
            setImage(currentAnimation[currentFrame]);
        }
        catch (Exception e)
        {
            System.out.println("Error loading boy sprites: " + e.getMessage());
            createFallbackImage();
        }
    }
    
    /**
     * Load frames from a horizontal spritesheet
     */
    private GreenfootImage[] loadDirectionalFrames(String spritesheetPath)
    {
        GreenfootImage spritesheet = new GreenfootImage(spritesheetPath);
        GreenfootImage[] frames = new GreenfootImage[FRAMES_PER_DIRECTION];
        
        for (int i = 0; i < FRAMES_PER_DIRECTION; i++)
        {
            // Extract each frame
            GreenfootImage frame = new GreenfootImage(FRAME_WIDTH, FRAME_HEIGHT);
            frame.drawImage(spritesheet, -i * FRAME_WIDTH, 0);
            
            // Scale to 3x size
            frame.scale(DISPLAY_WIDTH, DISPLAY_HEIGHT);
            frames[i] = frame;
        }
        
        return frames;
    }
    
    /**
     * Create a fallback image if spritesheets fail to load
     */
    private void createFallbackImage()
    {
        GreenfootImage image = new GreenfootImage(DISPLAY_WIDTH, DISPLAY_HEIGHT);
        image.setColor(new Color(0, 100, 200)); // Blue
        image.fillRect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT);
        image.setColor(Color.WHITE);
        image.fillOval(80, 50, 40, 40);
        image.fillOval(168, 50, 40, 40);
        setImage(image);
    }

    /**
     * Act - Handle keyboard input for movement and animation
     */
    public void act()
    {
        handleMovement();
        updateAnimation();
    }

    private void handleMovement()
    {
        isMoving = false;

        if (Greenfoot.isKeyDown("up"))
        {
            setLocation(getX(), getY() - speed);
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                setLocation(getX(), getY() + speed);
            }
            else
            {
                setDirection(0, upFrames);
                isMoving = true;
            }
        }
        else if (Greenfoot.isKeyDown("down"))
        {
            setLocation(getX(), getY() + speed);
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                setLocation(getX(), getY() - speed);
            }
            else
            {
                setDirection(2, downFrames);
                isMoving = true;
            }
        }
        else if (Greenfoot.isKeyDown("left"))
        {
            setLocation(getX() - speed, getY());
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                setLocation(getX() + speed, getY());
            }
            else
            {
                setDirection(1, leftFrames);
                isMoving = true;
            }
        }
        else if (Greenfoot.isKeyDown("right"))
        {
            setLocation(getX() + speed, getY());
            if (isTouching(Desk.class) || isTouching(Wall.class))
            {
                setLocation(getX() - speed, getY());
            }
            else
            {
                setDirection(3, rightFrames);
                isMoving = true;
            }
        }
        
        // If not moving, reset to first frame of current direction
        if (!isMoving)
        {
            currentFrame = 0;
            animationCounter = 0;
        }
    }
    
    /**
     * Set the current direction and animation
     */
    private void setDirection(int direction, GreenfootImage[] frames)
    {
        if (lastDirection != direction)
        {
            lastDirection = direction;
            currentAnimation = frames;
            currentFrame = 0;
            animationCounter = 0;
        }
    }
    
    /**
     * Update the animation frame
     */
    private void updateAnimation()
    {
        if (currentAnimation == null) return;
        
        if (isMoving)
        {
            animationCounter++;
            if (animationCounter >= FRAME_DELAY)
            {
                animationCounter = 0;
                currentFrame = (currentFrame + 1) % FRAMES_PER_DIRECTION;
            }
        }
        
        setImage(currentAnimation[currentFrame]);
    }
}
