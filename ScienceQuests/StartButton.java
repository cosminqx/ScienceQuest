import greenfoot.*;

public class StartButton extends Actor
{
    private String label;
    private boolean isHovered;

    public StartButton()
    {
        this("START");
    }

    public StartButton(String label)
    {
        this.label = label;
        this.isHovered = false;
        updateImage();
    }

    private void updateImage()
    {
        try
        {
            // Load the actor image
            GreenfootImage image = new GreenfootImage("Adobe Express - file.png");
            
            // Scale to reasonable button size while maintaining aspect ratio
            int maxWidth = 180;
            int maxHeight = 220;
            double aspectRatio = (double) image.getWidth() / image.getHeight();
            int scaledWidth, scaledHeight;
            
            if (aspectRatio > (double) maxWidth / maxHeight)
            {
                // Image is wider, fit to width
                scaledWidth = maxWidth;
                scaledHeight = (int) (maxWidth / aspectRatio);
            }
            else
            {
                // Image is taller, fit to height
                scaledHeight = maxHeight;
                scaledWidth = (int) (maxHeight * aspectRatio);
            }
            
            image.scale(scaledWidth, scaledHeight);
            
            // Add a subtle border effect when hovered
            if (isHovered)
            {
                GreenfootImage bordered = new GreenfootImage(scaledWidth + 10, scaledHeight + 10);
                bordered.setColor(new Color(255, 215, 0)); // Gold border
                bordered.fillRect(0, 0, scaledWidth + 10, scaledHeight + 10);
                bordered.drawImage(image, 5, 5);
                setImage(bordered);
            }
            else
            {
                setImage(image);
            }
        }
        catch (Exception e)
        {
            // Fallback if image not found
            GreenfootImage fallback = new GreenfootImage(200, 60);
            fallback.setColor(Color.DARK_GRAY);
            fallback.fillRect(0, 0, 200, 60);
            fallback.setColor(Color.WHITE);
            fallback.drawString(label, 50, 40);
            setImage(fallback);
        }
    }

    public void act()
    {
        // Check if mouse is hovering
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null && getDistance(mouse.getX(), mouse.getY()) < 80)
        {
            if (!isHovered)
            {
                isHovered = true;
                updateImage();
            }
        }
        else if (isHovered)
        {
            isHovered = false;
            updateImage();
        }
        
        if (Greenfoot.mouseClicked(this))
        {
            onButtonClicked();
        }
    }

    private double getDistance(int x, int y)
    {
        int dx = getX() - x;
        int dy = getY() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void onButtonClicked()
    {
        // Default behavior - override in subclasses if needed
        startGame();
    }

    public void startGame()
    {
        StartWorld world = (StartWorld) getWorld();
        world.showNameScreen();
    }
}
