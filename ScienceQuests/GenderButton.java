import greenfoot.*;

/**
 * GenderButton - Button for selecting player gender with character image
 */
public class GenderButton extends Actor
{
    private String gender;
    private boolean isSelected;
    private boolean isHovered;
    private GreenfootImage characterImage;
    private boolean usesSpritesheet = false;

    public GenderButton(String gender, String imagePath)
    {
        this.gender = gender;
        this.isSelected = false;
        this.isHovered = false;
        
        // Check if this is a spritesheet path
        if (imagePath.startsWith("spritesheet/"))
        {
            usesSpritesheet = true;
            loadSpriteFromSheet(imagePath);
        }
        else
        {
            // Load character image; keep GIFs unscaled to preserve animation per Greenfoot docs
            try
            {
                this.characterImage = new GreenfootImage(imagePath);
                String lower = imagePath.toLowerCase();
                if (!lower.endsWith(".gif"))
                {
                    this.characterImage.scale(140, 140);
                }
            }
            catch (Exception e)
            {
                this.characterImage = null;
            }
        }
        
        updateImage();
    }
    
    /**
     * Load the first frame from a spritesheet
     */
    private void loadSpriteFromSheet(String spritesheetPath)
    {
        try
        {
            GreenfootImage spritesheet = new GreenfootImage(spritesheetPath);
            int spriteWidth = 480;
            int spriteHeight = 320;
            
            // Extract first sprite (frame 0)
            GreenfootImage fullFrame = new GreenfootImage(spriteWidth, spriteHeight);
            fullFrame.drawImage(spritesheet, 0, 0);
            
            // Crop the center portion to fit button (80x80)
            int cropWidth = 140;
            int cropHeight = 140;
            int cropX = (spriteWidth - cropWidth) / 2;
            int cropY = (spriteHeight - cropHeight) / 2;
            
            this.characterImage = new GreenfootImage(cropWidth, cropHeight);
            this.characterImage.drawImage(fullFrame, -cropX, -cropY);
        }
        catch (Exception e)
        {
            this.characterImage = null;
        }
    }

    public void act()
    {
        // Check if mouse is hovering
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null && getDistance(mouse.getX(), mouse.getY()) < 60)
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
            isSelected = !isSelected;
            updateImage();
        }
    }

    private double getDistance(int x, int y)
    {
        int dx = getX() - x;
        int dy = getY() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void updateImage()
    {
        int buttonWidth = 140;
        int buttonHeight = 160;
        
        GreenfootImage image = new GreenfootImage(buttonWidth, buttonHeight);

        if (isSelected)
        {
            // Selected state - vibrant blue with gradient effect
            image.setColor(new Color(70, 130, 250));
            image.fillRect(0, 0, buttonWidth, buttonHeight);
            image.setColor(new Color(100, 150, 255));
            image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
        }
        else if (isHovered)
        {
            // Hovered state - light gray
            image.setColor(new Color(180, 200, 220));
            image.fillRect(0, 0, buttonWidth, buttonHeight);
            image.setColor(new Color(200, 220, 240));
            image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
        }
        else
        {
            // Default state - darker gray
            image.setColor(new Color(90, 110, 140));
            image.fillRect(0, 0, buttonWidth, buttonHeight);
            image.setColor(new Color(120, 140, 170));
            image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
        }

        // Border
        image.setColor(new Color(50, 70, 110));
        image.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
        
        // Draw character image if available
        if (characterImage != null)
        {
            // Center the character image
            int imgX = (buttonWidth - characterImage.getWidth()) / 2;
            int imgY = (buttonHeight - characterImage.getHeight() - 30) / 2 + 20; // Move down 20 pixels
            image.drawImage(characterImage, imgX, imgY);
        }
        
        // Text label at bottom
        image.setColor(Color.WHITE);
        image.drawString(gender, 25, 150);

        setImage(image);
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public String getGender()
    {
        return gender;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
        updateImage();
    }
}
    
