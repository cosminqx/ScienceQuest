import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Boy - Playable character that can be controlled with arrow keys
 * Uses directional spritesheets (UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT) with 8 frames each
 */
public class Boy extends BasePlayer
{
    // Directional spritesheets
    private GreenfootImage[] upFrames;
    private GreenfootImage[] downFrames;
    private GreenfootImage[] leftFrames;
    private GreenfootImage[] rightFrames;
     private GreenfootImage[] upLeftFrames;
     private GreenfootImage[] upRightFrames;
    
    // Animation state
    private GreenfootImage[] currentAnimation;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private static final int FRAME_DELAY = 5; // acts between frames
    
    // Frame dimensions
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 64;
    private static final int DISPLAY_WIDTH = 288;  // 3x scale
    private static final int DISPLAY_HEIGHT = 192; // 3x scale
    private static final int FRAMES_PER_DIRECTION = 8;
    private static final int CROP_WIDTH = 60;
    private static final int CROP_HEIGHT = 80;
    private static final int HITBOX_OFFSET_Y = 30; // Collision hitbox offset toward feet

    public Boy()
    {
        try
        {
            // Load all directional spritesheets
            upFrames = loadDirectionalFrames("spritesheet/boy/UP.png");
            downFrames = loadDirectionalFrames("spritesheet/boy/DOWN.png");
            leftFrames = loadDirectionalFrames("spritesheet/boy/LEFT.png");
            rightFrames = loadDirectionalFrames("spritesheet/boy/RIGHT.png");
            upLeftFrames = loadDirectionalFrames("spritesheet/boy/UP_LEFT.png");
            upRightFrames = loadDirectionalFrames("spritesheet/boy/UP_RIGHT.png");
            
            // Start with down-facing idle frame
            currentAnimation = downFrames;
            currentFrame = 0;
            setImage(currentAnimation[currentFrame]);
        }
        catch (Exception e)
        {
            DebugLog.log("Error loading boy sprites: " + e.getMessage());
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
            
            // Crop to tight hitbox centered on character
            int startX = (DISPLAY_WIDTH - CROP_WIDTH) / 2;
            int startY = (DISPLAY_HEIGHT - CROP_HEIGHT) / 2;
            GreenfootImage croppedFrame = new GreenfootImage(CROP_WIDTH, CROP_HEIGHT);
            croppedFrame.drawImage(frame, -startX, -startY);
            frames[i] = croppedFrame;
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
    protected int getHitboxWidth()
    {
        return CROP_WIDTH;
    }

    protected int getHitboxHeight()
    {
        return 18;
    }

    protected int getHitboxOffsetY()
    {
        return HITBOX_OFFSET_Y;
    }

    protected void onDirectionChanged(int newDirection)
    {
        GreenfootImage[] frames = null;
        switch (newDirection)
        {
            case DIR_UP:
                frames = upFrames;
                break;
            case DIR_DOWN:
                frames = downFrames;
                break;
            case DIR_LEFT:
                frames = leftFrames;
                break;
            case DIR_RIGHT:
                frames = rightFrames;
                break;
            case DIR_UP_LEFT:
                frames = upLeftFrames != null ? upLeftFrames : leftFrames;
                break;
            case DIR_UP_RIGHT:
                frames = upRightFrames != null ? upRightFrames : rightFrames;
                break;
            case DIR_DOWN_LEFT:
                frames = leftFrames;
                break;
            case DIR_DOWN_RIGHT:
                frames = rightFrames;
                break;
            default:
                frames = downFrames;
                break;
        }

        if (frames != null)
        {
            currentAnimation = frames;
            currentFrame = 0;
            animationCounter = 0;
        }
    }
    
    /**
     * Update the animation frame
     */
    protected void updateAnimation()
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
        else
        {
            currentFrame = 0;
            animationCounter = 0;
        }
        
        setImage(currentAnimation[currentFrame]);
    }
}
