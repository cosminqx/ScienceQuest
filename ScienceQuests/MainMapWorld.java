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
        Label greetingLabel = new Label("Hello, " + playerName + "!", 32);
        addObject(greetingLabel, getWidth()/2, 100);
        
        // Display gender information
        Label genderLabel = new Label("You are gender of " + playerGender + ".", 24);
        addObject(genderLabel, getWidth()/2, 150);
        
        // Main map placeholder
        Label mapLabel = new Label("Main Map (Prototype)", 30);
        addObject(mapLabel, getWidth()/2, getHeight()/2);
    }
}

