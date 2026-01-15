import greenfoot.*;
import java.util.*;

/**
 * AnimatedCharacter - Displays an animated character using spritesheet frames
 * Used for menu display with idle animation (frames 0-3)
 */
public class AnimatedCharacter extends Actor
{
    private GreenfootImage spritesheet;
    private List<FrameInfo> idleFrames;
    private int currentFrameIndex;
    private int animationCounter;
    private int frameWidth;
    private int frameHeight;
    private int displayWidth;
    private int displayHeight;
    private int cropWidth;
    private int cropHeight;
    private boolean useCrop;
    private int frameDuration; // milliseconds per frame

    public AnimatedCharacter(String spritesheetPath, int frameW, int frameH, 
                            int displayW, int displayH, int[] idleFrameIndices)
    {
        this(spritesheetPath, frameW, frameH, displayW, displayH, idleFrameIndices, 0, 0, 167);
    }

    public AnimatedCharacter(String spritesheetPath, int frameW, int frameH, 
                            int displayW, int displayH, int[] idleFrameIndices,
                            int cropW, int cropH)
    {
        this(spritesheetPath, frameW, frameH, displayW, displayH, idleFrameIndices, cropW, cropH, 167);
    }

    public AnimatedCharacter(String spritesheetPath, int frameW, int frameH, 
                            int displayW, int displayH, int[] idleFrameIndices,
                            int cropW, int cropH, int fps)
    {
        this.frameWidth = frameW;
        this.frameHeight = frameH;
        this.displayWidth = displayW;
        this.displayHeight = displayH;
        this.cropWidth = cropW;
        this.cropHeight = cropH;
        this.useCrop = (cropW > 0 && cropH > 0);
        this.currentFrameIndex = 0;
        this.animationCounter = 0;
        this.frameDuration = (fps > 0) ? (1000 / fps) : 167;
        this.idleFrames = new ArrayList<>();
        
        try
        {
            // Load the spritesheet image
            spritesheet = new GreenfootImage(spritesheetPath);
            
            // Initialize idle frame positions (frame 0-3, each 96x64, horizontally arranged)
            int[] frames = (idleFrameIndices != null) ? idleFrameIndices : new int[]{0, 1, 2, 3};
            
            for (int frameIdx : frames)
            {
                int xPos = frameIdx * frameW; // 0, 96, 192, 288
                int yPos = 0;
                idleFrames.add(new FrameInfo(xPos, yPos));
            }
            
            // Set initial image
            updateImage();
        }
        catch (Exception e)
        {
            System.out.println("Error loading character spritesheet: " + e.getMessage());
            createFallbackImage();
        }
    }

    /**
     * Create a fallback image if spritesheet fails to load
     */
    private void createFallbackImage()
    {
        GreenfootImage fallback = new GreenfootImage(displayWidth, displayHeight);
        fallback.setColor(Color.BLUE);
        fallback.fillRect(0, 0, displayWidth, displayHeight);
        setImage(fallback);
    }

    /**
     * Act method - update animation
     */
    public void act()
    {
        animationCounter++;
        
        // Update frame based on custom FPS (assuming ~60fps, divide by ~16)
        if (animationCounter >= frameDuration / 16)
        {
            animationCounter = 0;
            currentFrameIndex = (currentFrameIndex + 1) % idleFrames.size();
            updateImage();
        }
    }

    /**
     * Get the current frame image without updating animation
     */
    public GreenfootImage getCurrentFrame()
    {
        if (idleFrames.isEmpty() || spritesheet == null) return null;
        
        FrameInfo frame = idleFrames.get(currentFrameIndex);
        
        // Extract frame from spritesheet
        GreenfootImage frameImage = new GreenfootImage(frameWidth, frameHeight);
        frameImage.drawImage(spritesheet, -frame.x, -frame.y);
        
        // Crop centered portion if specified
        if (useCrop)
        {
            int cropX = (frameWidth - cropWidth) / 2;
            int cropY = (frameHeight - cropHeight) / 2;
            GreenfootImage croppedImage = new GreenfootImage(cropWidth, cropHeight);
            croppedImage.drawImage(frameImage, -cropX, -cropY);
            frameImage = croppedImage;
        }
        
        // Scale to display size
        frameImage.scale(displayWidth, displayHeight);
        
        return frameImage;
    }

    /**
     * Update the displayed image with the current frame
     */
    private void updateImage()
    {
        if (idleFrames.isEmpty() || spritesheet == null) return;
        
        FrameInfo frame = idleFrames.get(currentFrameIndex);
        
        // Extract frame from spritesheet
        GreenfootImage frameImage = new GreenfootImage(frameWidth, frameHeight);
        frameImage.drawImage(spritesheet, -frame.x, -frame.y);
        
        // Crop centered portion if specified
        if (useCrop)
        {
            int cropX = (frameWidth - cropWidth) / 2;
            int cropY = (frameHeight - cropHeight) / 2;
            GreenfootImage croppedImage = new GreenfootImage(cropWidth, cropHeight);
            croppedImage.drawImage(frameImage, -cropX, -cropY);
            frameImage = croppedImage;
        }
        
        // Scale to display size
        frameImage.scale(displayWidth, displayHeight);
        
        setImage(frameImage);
    }

    /**
     * Reset animation to first frame
     */
    public void resetAnimation()
    {
        currentFrameIndex = 0;
        animationCounter = 0;
        updateImage();
    }

    /**
     * Stop animation at current frame
     */
    public void stop()
    {
        animationCounter = 0;
    }

    /**
     * Inner class to store frame position data
     */
    private static class FrameInfo
    {
        int x, y;
        
        FrameInfo(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }
}
