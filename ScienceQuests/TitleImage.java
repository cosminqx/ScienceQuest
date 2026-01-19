import greenfoot.*;

/**
 * TitleImage - displays the title using a provided PNG asset with loading animation.
 */
public class TitleImage extends Actor
{
    private static final int MAX_WIDTH = 520;
    private static final int MAX_HEIGHT = 160;
    private GreenfootImage fullImage;
    private int totalFrames;
    private int currentFrame;
    private int scaledWidth;
    private int scaledHeight;

    public TitleImage()
    {
        try
        {
            fullImage = new GreenfootImage("fonts/696d62d95d863.png");
            scaleToFit(fullImage);
            scaledWidth = fullImage.getWidth();
            scaledHeight = fullImage.getHeight();
            
            // 36 seconds at 60 FPS = 2160 frames
            totalFrames = 36 * 60;
            currentFrame = 0;
            
            // Start with a minimal visible portion
            updateDisplay();
        }
        catch (Exception e)
        {
            // Fallback: simple gray bar
            GreenfootImage img = new GreenfootImage(MAX_WIDTH, 80);
            img.setColor(new Color(180, 180, 180));
            img.fillRect(0, 0, MAX_WIDTH, 80);
            setImage(img);
        }
    }
    
    public void act()
    {
        if (fullImage != null && currentFrame < totalFrames)
        {
            currentFrame++;
            updateDisplay();
        }
    }
    
    private void updateDisplay()
    {
        // Calculate progress (0.0 to 1.0)
        double progress = (double) currentFrame / totalFrames;
        int visibleWidth = Math.max(1, (int) (scaledWidth * progress));
        
        // Create display image with transparent background
        GreenfootImage display = new GreenfootImage(scaledWidth, scaledHeight);
        display.clear();
        
        // Draw only the visible portion (left side) of the full image
        GreenfootImage temp = new GreenfootImage(fullImage);
        display.drawImage(temp, 0, 0);
        
        // Mask out the right side that shouldn't be visible yet
        display.setColor(getWorld() != null ? getWorld().getBackground().getColor() : Color.BLACK);
        if (visibleWidth < scaledWidth)
        {
            display.fillRect(visibleWidth, 0, scaledWidth - visibleWidth, scaledHeight);
        }
        
        setImage(display);
    }

    private void scaleToFit(GreenfootImage img)
    {
        int w = img.getWidth();
        int h = img.getHeight();
        double scale = 1.0;

        if (w > MAX_WIDTH || h > MAX_HEIGHT)
        {
            double scaleW = (double) MAX_WIDTH / w;
            double scaleH = (double) MAX_HEIGHT / h;
            scale = Math.min(scaleW, scaleH);
        }

        int newW = (int) Math.round(w * scale);
        int newH = (int) Math.round(h * scale);
        img.scale(newW, newH);
    }
    
    public boolean isComplete()
    {
        return currentFrame >= totalFrames;
    }
}
