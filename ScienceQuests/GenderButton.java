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

    public GenderButton(String gender, String imagePath)
    {
        this.gender = gender;
        this.isSelected = false;
        this.isHovered = false;
        
        // Load character image; keep GIFs unscaled to preserve animation per Greenfoot docs
        try
        {
            this.characterImage = new GreenfootImage(imagePath);
            String lower = imagePath.toLowerCase();
            if (!lower.endsWith(".gif"))
            {
                this.characterImage.scale(80, 80);
            }
        }
        catch (Exception e)
        {
            this.characterImage = null;
        }
        
        updateImage();
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
        GreenfootImage image = new GreenfootImage(140, 160);

        if (isSelected)
        {
            // Selected state - vibrant blue with gradient effect
            image.setColor(new Color(70, 130, 250));
            image.fillRect(0, 0, 140, 160);
            image.setColor(new Color(100, 150, 255));
            image.fillRect(2, 2, 136, 156);
        }
        else if (isHovered)
        {
            // Hovered state - light gray
            image.setColor(new Color(180, 200, 220));
            image.fillRect(0, 0, 140, 160);
            image.setColor(new Color(200, 220, 240));
            image.fillRect(2, 2, 136, 156);
        }
        else
        {
            // Default state - darker gray
            image.setColor(new Color(90, 110, 140));
            image.fillRect(0, 0, 140, 160);
            image.setColor(new Color(120, 140, 170));
            image.fillRect(2, 2, 136, 156);
        }

        // Border
        image.setColor(new Color(50, 70, 110));
        image.drawRect(0, 0, 139, 159);
        
        // Draw character image if available
        if (characterImage != null)
        {
            image.drawImage(characterImage, 30, 20);
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
    
