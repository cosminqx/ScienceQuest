import greenfoot.*;

/**
 * GenderButton - Button for selecting player gender with character image
 */
public class GenderButton extends Actor
{
    private String gender;
    private boolean isSelected;
    private boolean isHovered;
    private static GenderButton selectedButton = null;
    private GreenfootImage characterImage;
    private boolean usesSpritesheet = false;
    private AnimatedCharacter animatedCharacter = null;
    private boolean isAnimated = false;

    public GenderButton(String gender, String imagePath)
    {
        this.gender = gender;
        this.isSelected = false;
        this.isHovered = false;
        
        // Check if this is a spritesheet path with animation
        if (imagePath.equals("spritesheet/boy/animated"))
        {
            isAnimated = true;
            // Create animated character for boy (frames 0-3, 192x128 size)
            // Crop centered 122x143 portion to make character bigger, display at 122x143
            animatedCharacter = new AnimatedCharacter(
                "spritesheet/boy/idle_simple.png",
                192,
                128,
                122,
                143,
                new int[]{0, 1, 2, 3},
                45,  // crop width - centered portion
                48,  // crop height - full height
                10   // 10 FPS
            );
        }
        else if (imagePath.equals("spritesheet/girl/animated"))
        {
            isAnimated = true;
            // Create animated character for girl (9 frames, 96x64 per frame, 6x scale = 576x384, 20 FPS)
            animatedCharacter = new AnimatedCharacter(
                "spritesheet/girl/idle.png",
                96,
                64,
                576,
                384,
                new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8},
                0,
                0,
                20  // 20 FPS
            );
        }
        else if (imagePath.startsWith("spritesheet/"))
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
            // Update animation ONLY when hovering
            if (isAnimated && animatedCharacter != null)
            {
                animatedCharacter.act();
                updateImage(); // Update image to show new frame
            }
        }
        else if (isHovered)
        {
            isHovered = false;
            // Reset animation to first frame when not hovering
            if (isAnimated && animatedCharacter != null)
            {
                animatedCharacter.resetAnimation();
            }
            updateImage();
        }
        
        if (Greenfoot.mouseClicked(this))
        {
            // Enforce single selection across all gender buttons
            if (selectedButton != this)
            {
                if (selectedButton != null)
                {
                    selectedButton.setSelected(false);
                }
                selectedButton = this;
                setSelected(true);
            }
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
        int buttonWidth = 150;
        int buttonHeight = 180;
        
        GreenfootImage image = new GreenfootImage(buttonWidth, buttonHeight);

        // Use custom color for Male button
        boolean isMaleButton = gender.equals("Alex");
        boolean isFemaleButton = gender.equals("Maria");

        if (isSelected)
        {
            // Selected state - lighter red
            if (isMaleButton || isFemaleButton)
            {
                image.setColor(new Color(255, 100, 115)); // lighter red
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(255, 130, 145));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
            else
            {
                image.setColor(new Color(70, 130, 250));
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(100, 150, 255));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
        }
        else if (isHovered)
        {
            // Hovered state - darker red
            if (isMaleButton || isFemaleButton)
            {
                image.setColor(new Color(180, 30, 45)); // darker red
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(210, 50, 65));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
            else
            {
                image.setColor(new Color(180, 200, 220));
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(200, 220, 240));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
        }
        else
        {
            // Default state - normal red
            if (isMaleButton || isFemaleButton)
            {
                image.setColor(new Color(233, 50, 69)); // #e93245
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(240, 80, 95));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
            else
            {
                image.setColor(new Color(90, 110, 140));
                image.fillRect(0, 0, buttonWidth, buttonHeight);
                image.setColor(new Color(120, 140, 170));
                image.fillRect(2, 2, buttonWidth - 4, buttonHeight - 4);
            }
        }

        // Border
        image.setColor(new Color(50, 70, 110));
        image.drawRect(0, 0, buttonWidth - 1, buttonHeight - 1);
        
        // Draw character image if available
        if (isAnimated && animatedCharacter != null)
        {
            // Get the current frame from animated character
            GreenfootImage currentFrame = animatedCharacter.getCurrentFrame();
            if (currentFrame != null)
            {
                int imgX = (buttonWidth - currentFrame.getWidth()) / 2;
                int imgY = (buttonHeight - currentFrame.getHeight() - 30) / 2 + 20;
                image.drawImage(currentFrame, imgX, imgY);
            }
        }
        else if (characterImage != null)
        {
            // Center the character image
            int imgX = (buttonWidth - characterImage.getWidth()) / 2;
            int imgY = (buttonHeight - characterImage.getHeight() - 30) / 2 + 20; // Move down 20 pixels
            image.drawImage(characterImage, imgX, imgY);
        }
        
        // Text label at bottom, centered
        GreenfootImage textImg = new GreenfootImage(gender, 24, Color.WHITE, new Color(0, 0, 0, 0));
        int textX = (buttonWidth - textImg.getWidth()) / 2;
        int textY = buttonHeight - textImg.getHeight() - 8;
        image.drawImage(textImg, textX, textY);

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
    
