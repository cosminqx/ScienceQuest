import greenfoot.*;

/**
 * TitleImage - displays the title using a provided PNG asset.
 */
public class TitleImage extends Actor
{
    private static final int MAX_WIDTH = 520;
    private static final int MAX_HEIGHT = 160;

    public TitleImage()
    {
        try
        {
            GreenfootImage fullImage = new GreenfootImage("fonts/696d62d95d863.png");
            scaleToFit(fullImage);
            setImage(fullImage);
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
        // No animation needed
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
}

