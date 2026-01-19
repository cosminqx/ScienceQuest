import greenfoot.*;

/**
 * LoadingBar - displays a horizontal loading animation over 36 seconds
 */
public class LoadingBar extends Actor
{
    private GreenfootImage fullImage;
    private int totalFrames;
    private int currentFrame;
    private int maxWidth;
    
    public LoadingBar()
    {
        // Load the image
        fullImage = new GreenfootImage("fonts/696d62d95d863.png");
        maxWidth = fullImage.getWidth();
        
        // 36 seconds at 60 FPS = 2160 frames
        totalFrames = 36 * 60;
        currentFrame = 0;
        
        // Start with empty image
        updateDisplay();
    }
    
    public void act()
    {
        if (currentFrame < totalFrames)
        {
            currentFrame++;
            updateDisplay();
        }
    }
    
    private void updateDisplay()
    {
        // Calculate how much of the image to show
        double progress = (double) currentFrame / totalFrames;
        int currentWidth = (int) (maxWidth * progress);
        
        if (currentWidth < 1) currentWidth = 1;
        if (currentWidth > maxWidth) currentWidth = maxWidth;
        
        // Create an image showing only the left portion
        GreenfootImage display = new GreenfootImage(maxWidth, fullImage.getHeight());
        display.setColor(new Color(0, 0, 0, 0)); // Transparent
        display.fill();
        
        // Copy the left portion of the full image
        for (int x = 0; x < currentWidth; x++)
        {
            for (int y = 0; y < fullImage.getHeight(); y++)
            {
                display.setColorAt(x, y, fullImage.getColorAt(x, y));
            }
        }
        
        setImage(display);
    }
    
    public boolean isComplete()
    {
        return currentFrame >= totalFrames;
    }
}
