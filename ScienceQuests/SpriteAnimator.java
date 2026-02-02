import greenfoot.*;
import java.util.*;

/**
 * SpriteAnimator - Handles sprite sheet animation with frame management
 * Parses JSON spritesheet data and manages animation frames
 */
public class SpriteAnimator
{
    private GreenfootImage spritesheet;
    private List<FrameData> frames;
    private int currentFrameIndex;
    private int animationCounter;
    private boolean isAnimating;
    private int targetWidth;
    private int targetHeight;
    private int rangeStart;
    private int rangeEnd;

    public SpriteAnimator(String spritesheetPath, String jsonPath, int width, int height)
    {
        this.frames = new ArrayList<>();
        this.currentFrameIndex = 0;
        this.animationCounter = 0;
        this.isAnimating = true;
        this.targetWidth = width;
        this.targetHeight = height;
        this.rangeStart = 0;
        this.rangeEnd = -1; // -1 means full range
        
        try
        {
            // Load spritesheet image
            this.spritesheet = new GreenfootImage(spritesheetPath);
            
            // Parse JSON and extract frame data
            parseJsonFrameData(jsonPath);
        }
        catch (Exception e)
        {
            DebugLog.log("Error loading spritesheet: " + e.getMessage());
        }
    }

    /**
     * Parse the JSON spritesheet metadata file
     */
    private void parseJsonFrameData(String jsonPath)
    {
        try
        {
            java.nio.file.Path path = java.nio.file.Paths.get(jsonPath);
            String content = new String(java.nio.file.Files.readAllBytes(path));
            
            // Extract frames data from JSON
            int framesStart = content.indexOf("\"frames\"");
            int framesEnd = content.lastIndexOf("}");
            
            if (framesStart == -1) return;
            
            // Parse each frame entry
            String framesContent = content.substring(framesStart, framesEnd);
            
            // Extract frame objects by looking for the pattern
            int index = 0;
            while (true)
            {
                int frameStart = framesContent.indexOf("\"frame\"", index);
                if (frameStart == -1) break;
                
                int xStart = framesContent.indexOf("\"x\"", frameStart);
                int yStart = framesContent.indexOf("\"y\"", frameStart);
                int wStart = framesContent.indexOf("\"w\"", frameStart);
                int hStart = framesContent.indexOf("\"h\"", frameStart);
                
                if (xStart == -1 || yStart == -1 || wStart == -1 || hStart == -1) break;
                
                int x = Integer.parseInt(extractNumber(framesContent, xStart));
                int y = Integer.parseInt(extractNumber(framesContent, yStart));
                int w = Integer.parseInt(extractNumber(framesContent, wStart));
                int h = Integer.parseInt(extractNumber(framesContent, hStart));
                
                int durationStart = framesContent.indexOf("\"duration\"", hStart);
                int duration = 200; // default
                if (durationStart != -1)
                {
                    duration = Integer.parseInt(extractNumber(framesContent, durationStart));
                }
                
                frames.add(new FrameData(x, y, w, h, duration));
                
                index = frameStart + 1;
            }
            
            DebugLog.log("Loaded " + frames.size() + " frames from spritesheet");
        }
        catch (Exception e)
        {
            DebugLog.log("Error parsing JSON: " + e.getMessage());
        }
    }

    /**
     * Extract a number from JSON string at given position
     */
    private String extractNumber(String content, int pos)
    {
        int colonPos = content.indexOf(":", pos);
        int numStart = colonPos + 1;
        
        // Skip whitespace
        while (numStart < content.length() && Character.isWhitespace(content.charAt(numStart)))
        {
            numStart++;
        }
        
        int numEnd = numStart;
        while (numEnd < content.length() && Character.isDigit(content.charAt(numEnd)))
        {
            numEnd++;
        }
        
        return content.substring(numStart, numEnd);
    }

    /**
     * Get the current frame image
     */
    public GreenfootImage getCurrentFrame()
    {
        if (frames.isEmpty()) return null;
        
        FrameData frameData = frames.get(currentFrameIndex);
        
        // Extract the frame from the spritesheet
        GreenfootImage frameImage = new GreenfootImage(frameData.width, frameData.height);
        frameImage.drawImage(spritesheet, -frameData.x, -frameData.y);
        
        // Scale to target size
        frameImage.scale(targetWidth, targetHeight);
        
        return frameImage;
    }

    /**
     * Update animation frame based on duration
     */
    public void update()
    {
        if (!isAnimating || frames.isEmpty()) return;

        // Ensure currentFrameIndex is within active range
        int start = (rangeEnd >= 0) ? rangeStart : 0;
        int end = (rangeEnd >= 0) ? rangeEnd : frames.size() - 1;
        if (currentFrameIndex < start || currentFrameIndex > end)
        {
            currentFrameIndex = start;
        }

        FrameData currentFrame = frames.get(currentFrameIndex);
        animationCounter += 1; // Increment by 1 per act cycle

        if (animationCounter >= currentFrame.duration / 16) // ~60fps, so divide by ~16
        {
            animationCounter = 0;
            if (currentFrameIndex >= end)
            {
                currentFrameIndex = start;
            }
            else
            {
                currentFrameIndex++;
            }
        }
    }

    /**
     * Set which frames to animate (e.g., frames 0-3 for idle animation)
     */
    public void setFrameRange(int startFrame, int endFrame)
    {
        if (frames.isEmpty()) return;
        if (startFrame < 0) startFrame = 0;
        if (endFrame >= frames.size()) endFrame = frames.size() - 1;
        if (startFrame > endFrame) return;

        this.rangeStart = startFrame;
        this.rangeEnd = endFrame;
        this.currentFrameIndex = startFrame;
        this.animationCounter = 0;
    }

    /**
     * Play animation
     */
    public void play()
    {
        isAnimating = true;
    }

    /**
     * Stop animation
     */
    public void stop()
    {
        isAnimating = false;
    }

    /**
     * Set current frame index
     */
    public void setFrame(int frameIndex)
    {
        if (frameIndex >= 0 && frameIndex < frames.size())
        {
            currentFrameIndex = frameIndex;
            animationCounter = 0;
        }
    }

    /**
     * Get total number of frames parsed from the json
     */
    public int getFrameCount()
    {
        return frames.size();
    }

    /**
     * Inner class to store frame metadata
     */
    private static class FrameData
    {
        int x, y, width, height, duration;
        
        FrameData(int x, int y, int w, int h, int duration)
        {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.duration = duration;
        }
    }
}
