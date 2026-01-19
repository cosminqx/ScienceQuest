import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Girl - Playable character with 8-direction movement
 * Uses LEFT and RIGHT walking animations, faking UP/DOWN directions with visual effects
 * 
 * Visual Tricks:
 * - UP movement: scales slightly smaller vertically + darker (simulates moving away)
 * - DOWN movement: scales slightly larger vertically + brighter (simulates coming closer)
 * - This creates depth perception in top-down pixel art without needing separate sprites
 */
public class Girl extends Actor
{
    // Direction constants
    private static final int DIR_UP = 0;
    private static final int DIR_LEFT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_RIGHT = 3;
    private static final int DIR_UP_LEFT = 4;
    private static final int DIR_UP_RIGHT = 5;
    private static final int DIR_DOWN_LEFT = 6;
    private static final int DIR_DOWN_RIGHT = 7;
    
    private int speed = 3;
    
    // Base frames (never modified - kept clean for visual transformations)
    private GreenfootImage[] baseLeftFrames;
    private GreenfootImage[] baseRightFrames;
    
    // Animation state
    private int currentDirection = DIR_LEFT;
    private int currentFrame = 0;
    private int animationCounter = 0;
    private static final int FRAME_DELAY = 5;
    private boolean isMoving = false;
    
    // Frame dimensions
    private static final int FRAME_WIDTH = 96;
    private static final int FRAME_HEIGHT = 64;
    private static final int BASE_DISPLAY_WIDTH = 288;  // 3x scale
    private static final int BASE_DISPLAY_HEIGHT = 192; // 3x scale
    private static final int FRAMES_PER_DIRECTION = 8;
    private static final int CROP_WIDTH = 80;      // Cropped collision box width
    private static final int CROP_HEIGHT = 100;    // Cropped collision box height
    private static final int HITBOX_OFFSET_Y = 30; // Collision hitbox offset toward feet
    
    // Visual effect parameters for depth simulation
    private static final double UP_VERTICAL_SCALE = 0.96;    // 96% height (moving away)
    private static final double DOWN_VERTICAL_SCALE = 1.04;  // 104% height (coming closer)
    private static final int UP_BRIGHTNESS_ADJUST = -15;     // Darker when moving away
    private static final int DOWN_BRIGHTNESS_ADJUST = 15;    // Brighter when coming closer

    public Girl()
    {
        try
        {
            // Load base frames (kept pristine for transformations)
            baseLeftFrames = loadDirectionalFrames("spritesheet/girl/LEFT.png");
            baseRightFrames = loadDirectionalFrames("spritesheet/girl/RIGHT.png");
            
            // Start with left-facing idle frame
            currentDirection = DIR_LEFT;
            currentFrame = 0;
            updateDisplayImage();
        }
        catch (Exception e)
        {
            System.out.println("Error loading girl sprites: " + e.getMessage());
            createFallbackImage();
        }
    }
    
    /**
     * Load frames from a horizontal spritesheet
     * These are kept as base frames and copied when applying visual effects
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
            frame.scale(BASE_DISPLAY_WIDTH, BASE_DISPLAY_HEIGHT);
            
            // Crop to remove transparent areas - center the character
            int startX = (int)Math.round(BASE_DISPLAY_WIDTH / 2 - CROP_WIDTH / 2);
            int startY = (int)Math.round(BASE_DISPLAY_HEIGHT / 2 - CROP_HEIGHT / 2);
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
        GreenfootImage image = new GreenfootImage(BASE_DISPLAY_WIDTH, BASE_DISPLAY_HEIGHT);
        image.setColor(new Color(255, 100, 200)); // Pink
        image.fillRect(0, 0, BASE_DISPLAY_WIDTH, BASE_DISPLAY_HEIGHT);
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

    /**
     * Handle keyboard input and determine movement direction
     * Separates movement logic from visual/animation logic
     */
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
        int newDirection = currentDirection;

        // Determine direction and movement (8 directions)
        if (up && left)
        {
            newY -= speed;
            newX -= speed;
            newDirection = DIR_UP_LEFT;
        }
        else if (up && right)
        {
            newY -= speed;
            newX += speed;
            newDirection = DIR_UP_RIGHT;
        }
        else if (down && left)
        {
            newY += speed;
            newX -= speed;
            newDirection = DIR_DOWN_LEFT;
        }
        else if (down && right)
        {
            newY += speed;
            newX += speed;
            newDirection = DIR_DOWN_RIGHT;
        }
        else if (up)
        {
            newY -= speed;
            newDirection = DIR_UP;
        }
        else if (down)
        {
            newY += speed;
            newDirection = DIR_DOWN;
        }
        else if (left)
        {
            newX -= speed;
            newDirection = DIR_LEFT;
        }
        else if (right)
        {
            newX += speed;
            newDirection = DIR_RIGHT;
        }

        // Update direction if changed
        if (newDirection != currentDirection)
        {
            currentDirection = newDirection;
            currentFrame = 0;
            animationCounter = 0;
        }

        // Attempt movement - only if actually moved
        if (newX != startX || newY != startY)
        {
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
                    }
                    else
                    {
                        // Try moving only vertically
                        int yOnlyMapX = world.screenToMapX(startX);
                        int yOnlyMapY = world.screenToMapY(newY + HITBOX_OFFSET_Y);
                        boolean yMoveCollides = world.isCollisionAt(yOnlyMapX, yOnlyMapY, CROP_WIDTH, 18);
                        
                        if (!yMoveCollides && newY != startY)
                        {
                            // Can slide vertically
                            setLocation(startX, newY);
                            isMoving = true;
                        }
                        else
                        {
                            // Completely blocked
                            isMoving = false;
                        }
                    }
                }
                else
                {
                    // No collision, move freely
                    setLocation(newX, newY);
                    isMoving = true;
                }
            }
            else
            {
                setLocation(newX, newY);
                isMoving = true;
            }
        }

        // If not moving, reset to first frame (idle pose)
        if (!isMoving)
        {
            currentFrame = 0;
            animationCounter = 0;
        }
    }
    
    /**
     * Update animation frame counter and cycle through frames
     */
    private void updateAnimation()
    {
        if (isMoving)
        {
            animationCounter++;
            if (animationCounter >= FRAME_DELAY)
            {
                animationCounter = 0;
                currentFrame = (currentFrame + 1) % FRAMES_PER_DIRECTION;
            }
        }
        
        updateDisplayImage();
    }
    
    /**
     * Apply the current frame with visual effects based on direction
     * Fakes UP/DOWN movement by transforming LEFT/RIGHT animations
     */
    private void updateDisplayImage()
    {
        if (baseLeftFrames == null || baseRightFrames == null) return;
        
        // Get the base frame (LEFT or RIGHT)
        GreenfootImage baseFrame = getBaseFrameForDirection();
        if (baseFrame == null) return;
        
        // Create a copy to apply effects
        GreenfootImage displayFrame = new GreenfootImage(baseFrame);
        
        // Apply visual effects based on direction
        applyDirectionEffects(displayFrame);
        
        setImage(displayFrame);
    }
    
    /**
     * Get the base LEFT or RIGHT frame for the current direction
     */
    private GreenfootImage getBaseFrameForDirection()
    {
        switch (currentDirection)
        {
            case DIR_LEFT:
            case DIR_UP_LEFT:
            case DIR_DOWN_LEFT:
            case DIR_UP:  // Pure vertical uses LEFT as base
            case DIR_DOWN:
                return baseLeftFrames[currentFrame];
                
            case DIR_RIGHT:
            case DIR_UP_RIGHT:
            case DIR_DOWN_RIGHT:
                return baseRightFrames[currentFrame];
                
            default:
                return baseLeftFrames[currentFrame];
        }
    }
    
    /**
     * Apply visual effects to fake UP/DOWN movement
     * - UP: scale smaller vertically + darken
     * - DOWN: scale larger vertically + brighten
     */
    private void applyDirectionEffects(GreenfootImage image)
    {
        switch (currentDirection)
        {
            case DIR_UP:
            case DIR_UP_LEFT:
            case DIR_UP_RIGHT:
                // Moving away - smaller and darker
                int upHeight = (int)(CROP_HEIGHT * UP_VERTICAL_SCALE);
                image.scale(CROP_WIDTH, upHeight);
                adjustBrightness(image, UP_BRIGHTNESS_ADJUST);
                break;
                
            case DIR_DOWN:
            case DIR_DOWN_LEFT:
            case DIR_DOWN_RIGHT:
                // Moving closer - larger and brighter
                int downHeight = (int)(CROP_HEIGHT * DOWN_VERTICAL_SCALE);
                image.scale(CROP_WIDTH, downHeight);
                adjustBrightness(image, DOWN_BRIGHTNESS_ADJUST);
                break;
                
            case DIR_LEFT:
            case DIR_RIGHT:
                // No effects for pure horizontal movement
                break;
        }
    }
    
    /**
     * Adjust image brightness by adding/subtracting from RGB values
     * Positive adjustment = brighter, negative = darker
     */
    private void adjustBrightness(GreenfootImage image, int adjustment)
    {
        if (adjustment == 0) return;
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                Color color = image.getColorAt(x, y);
                
                // Skip fully transparent pixels
                if (color.getAlpha() == 0) continue;
                
                // Adjust RGB values, clamping to 0-255
                int r = clamp(color.getRed() + adjustment, 0, 255);
                int g = clamp(color.getGreen() + adjustment, 0, 255);
                int b = clamp(color.getBlue() + adjustment, 0, 255);
                
                image.setColorAt(x, y, new Color(r, g, b, color.getAlpha()));
            }
        }
    }
    
    /**
     * Clamp a value between min and max
     */
    private int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(max, value));
    }
}
