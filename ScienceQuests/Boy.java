import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Boy - Playable character that can be controlled with arrow keys
 * Uses directional spritesheets (UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT) with 8 frames each
 */
public class Boy extends Actor
{
    private int speed = 3;
    
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
    private boolean isMoving = false;
     // Direction indices: 0=up,1=left,2=down,3=right,4=up-left,5=up-right,6=down-left,7=down-right
     private int lastDirection = 2;
    
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
    public void act()
    {
        // Only allow movement if no dialogue is active
        if (!DialogueManager.getInstance().isDialogueActive())
        {
            handleMovement();
        }
        updateAnimation();
    }

    private void handleMovement()
    {
        isMoving = false;

        boolean up = Greenfoot.isKeyDown("up");
        boolean down = Greenfoot.isKeyDown("down");
        boolean left = Greenfoot.isKeyDown("left");
        boolean right = Greenfoot.isKeyDown("right");

        // Neutralize opposing keys
        if (up && down) { up = false; down = false; }
        if (left && right) { left = false; right = false; }

        int startX = getX();
        int startY = getY();
        int newX = startX;
        int newY = startY;
        int chosenDir = lastDirection;
        GreenfootImage[] chosenFrames = currentAnimation;

        if (up && left)
        {
            newY -= speed;
            newX -= speed;
            chosenDir = 4;
            chosenFrames = upLeftFrames != null ? upLeftFrames : leftFrames;
        }
        else if (up && right)
        {
            newY -= speed;
            newX += speed;
            chosenDir = 5;
            chosenFrames = upRightFrames != null ? upRightFrames : rightFrames;
        }
        else if (down && left)
        {
            newY += speed;
            newX -= speed;
            chosenDir = 6;
            chosenFrames = leftFrames;
        }
        else if (down && right)
        {
            newY += speed;
            newX += speed;
            chosenDir = 7;
            chosenFrames = rightFrames;
        }
        else if (up)
        {
            newY -= speed;
            chosenDir = 0;
            chosenFrames = upFrames;
        }
        else if (down)
        {
            newY += speed;
            chosenDir = 2;
            chosenFrames = downFrames;
        }
        else if (left)
        {
            newX -= speed;
            chosenDir = 1;
            chosenFrames = leftFrames;
        }
        else if (right)
        {
            newX += speed;
            chosenDir = 3;
            chosenFrames = rightFrames;
        }

        if (chosenDir != lastDirection)
        {
            setDirection(chosenDir, chosenFrames);
        }

        // Check collision using feet rectangle with sliding collision
        MainMapWorld world = (MainMapWorld) getWorld();
        if (world != null)
        {
            int newMapX = world.screenToMapX(newX);
            int newMapY = world.screenToMapY(newY + HITBOX_OFFSET_Y);
            
            // Feet hitbox: full character width, 18px height
            boolean fullMoveCollides = world.isCollisionAt(newMapX, newMapY, CROP_WIDTH, 18);
            
            if (fullMoveCollides)
            {
                // Try moving only horizontally
                int xOnlyMapX = world.screenToMapX(newX);
                int xOnlyMapY = world.screenToMapY(startY + HITBOX_OFFSET_Y);
                boolean xMoveCollides = world.isCollisionAt(xOnlyMapX, xOnlyMapY, CROP_WIDTH, 18);
                
                if (!xMoveCollides && newX != startX)
                {
                    // Can slide horizontally
                    setLocation(newX, startY);
                    isMoving = true;
                    return;
                }
                
                // Try moving only vertically
                int yOnlyMapX = world.screenToMapX(startX);
                int yOnlyMapY = world.screenToMapY(newY + HITBOX_OFFSET_Y);
                boolean yMoveCollides = world.isCollisionAt(yOnlyMapX, yOnlyMapY, CROP_WIDTH, 18);
                
                if (!yMoveCollides && newY != startY)
                {
                    // Can slide vertically
                    setLocation(startX, newY);
                    isMoving = true;
                    return;
                }
                
                // Completely blocked
                isMoving = false;
                currentFrame = 0;
                animationCounter = 0;
                return;
            }
        }

        // No collision; apply movement
        setLocation(newX, newY);

        // Mark moving if any directional key was active
        isMoving = up || down || left || right;

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
        if (frames == null) return;
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
