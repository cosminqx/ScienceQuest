import greenfoot.*;

public class MainMapWorld extends World
{
    private Actor character;
    private GreenfootImage backgroundImage;
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;

    public MainMapWorld()
    {
        super(600, 400, 1);
        
        // Load and set up the background image
        setUpBackground();
        
        // Retrieve player data
        String playerName = PlayerData.getPlayerName();
        String playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender
        if ("Male".equals(playerGender))
        {
            Boy boy = new Boy();
            addObject(boy, getWidth()/2, getHeight()/2);
            character = boy;
        }
        else if ("Female".equals(playerGender))
        {
            Girl girl = new Girl();
            addObject(girl, getWidth()/2, getHeight()/2);
            character = girl;
        }
        else
        {
            // Default to boy for "Other" or unknown gender
            Boy boy = new Boy();
            addObject(boy, getWidth()/2, getHeight()/2);
            character = boy;
        }
        
        // Instructions
        Label instructionsLabel = new Label("Use arrow keys to move", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
    
    private void setUpBackground()
    {
        try
        {
            backgroundImage = new GreenfootImage("images/floor.png");
        }
        catch (Exception e)
        {
            // Fallback: create a simple green background if image not found
            backgroundImage = new GreenfootImage(getWidth(), getHeight());
            backgroundImage.setColor(new Color(34, 139, 34)); // Forest green
            backgroundImage.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Calculate max scroll values to prevent scrolling past the edges
        maxScrollX = Math.max(0, backgroundImage.getWidth() - getWidth());
        maxScrollY = Math.max(0, backgroundImage.getHeight() - getHeight());
    }
    
    public void act()
    {
        // Update the camera position to keep the character centered
        if (character != null && character.getWorld() != null)
        {
            // Calculate scroll position to center the character
            scrollX = character.getX() - getWidth() / 2;
            scrollY = character.getY() - getHeight() / 2;
            
            // Clamp scroll values to prevent viewing beyond the background
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            // Draw the background image with the scroll offset
            GreenfootImage worldImage = getBackground();
            worldImage.drawImage(backgroundImage, -scrollX, -scrollY);
        }
    }
}

