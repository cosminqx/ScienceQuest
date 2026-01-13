import greenfoot.*;

public class MainMapWorld extends World
{
    public MainMapWorld()
    {
        super(600, 400, 1);
        
        // Retrieve player data
        String playerName = PlayerData.getPlayerName();
        String playerGender = PlayerData.getPlayerGender();
        
        // Display greeting with player name
        Label greetingLabel = new Label("Hello, " + playerName + "!", 32, Color.WHITE);
        addObject(greetingLabel, getWidth()/2, 30);
        
        // Display gender information
        Label genderLabel = new Label("You are " + playerGender, 24, Color.WHITE);
        addObject(genderLabel, getWidth()/2, 60);
        
        // Spawn the correct character based on gender
        if ("Male".equals(playerGender))
        {
            Boy boy = new Boy();
            addObject(boy, getWidth()/2, getHeight()/2);
        }
        else if ("Female".equals(playerGender))
        {
            Girl girl = new Girl();
            // Ensure the main world uses the original avatar image for female selection
            try
            {
                GreenfootImage avatar = new GreenfootImage("avatarfata.png");
                avatar.scale(50, 60);
                girl.setImage(avatar);
            }
            catch (Exception e)
            {
                // If loading fails, Girl keeps its constructor image
            }
            addObject(girl, getWidth()/2, getHeight()/2);
        }
        else
        {
            // Default to boy for "Other" or unknown gender
            Boy boy = new Boy();
            addObject(boy, getWidth()/2, getHeight()/2);
        }
        
        // Instructions
        Label instructionsLabel = new Label("Use arrow keys to move", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
}

