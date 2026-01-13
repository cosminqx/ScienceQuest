import greenfoot.*;

public class StartButton extends Actor
{
    private String label;
    private boolean isHovered;
    private int baseY;
    private int hoverOffset = 10; // How many pixels to move up when hovering
    private int imageWidth;
    private int imageHeight;

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
            
            // Store the image dimensions for hitbox calculation
            this.imageWidth = scaledWidth;
            this.imageHeight = scaledHeight;
            
            setImage(image);
        }
        catch (Exception e)
        {
            // Fallback if image not found
            GreenfootImage fallback = new GreenfootImage(200, 60);
            fallback.setColor(Color.DARK_GRAY);
            fallback.fillRect(0, 0, 200, 60);
            fallback.setColor(Color.WHITE);
            fallback.drawString(label, 50, 40);
            
            this.imageWidth = 200;
            this.imageHeight = 60;
            
            setImage(fallback);
        }
    }

    public void act()
    {
        // Store base Y position on first frame
        if (baseY == 0)
        {
            baseY = getY();
        }
        
        // Check if mouse is hovering using image dimensions as hitbox
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null && isMouseOnButton(mouse.getX(), mouse.getY()))
        {
            if (!isHovered)
            {
                isHovered = true;
            }
        }
        else if (isHovered)
        {
            isHovered = false;
        }
        
        // Animate position based on hover state
        if (isHovered)
        {
            // Move up when hovering
            setLocation(getX(), baseY - hoverOffset);
        }
        else
        {
            // Return to original position
            setLocation(getX(), baseY);
        }
        
        if (Greenfoot.mouseClicked(this))
        {
            onButtonClicked();
        }
    }

    /**
     * Check if mouse is within the button's image bounds
     */
    private boolean isMouseOnButton(int mouseX, int mouseY)
    {
        int actorX = getX();
        int actorY = getY();
        
        // Calculate button bounds (actor position is at center)
        int left = actorX - imageWidth / 2;
        int right = actorX + imageWidth / 2;
        int top = actorY - imageHeight / 2;
        int bottom = actorY + imageHeight / 2;
        
        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
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
