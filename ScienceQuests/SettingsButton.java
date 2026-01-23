import greenfoot.*;

/**
 * SettingsButton - Clickable settings icon in the top-right corner
 */
public class SettingsButton extends Actor
{
    public SettingsButton()
    {
        // Load the 16x16 settings icon and scale it 3x
        GreenfootImage icon = new GreenfootImage("settings.png");
        icon.scale(48, 48);
        setImage(icon);
    }
    
    public void act()
    {
        // Check if clicked
        if (Greenfoot.mouseClicked(this))
        {
            openSettings();
        }
    }
    
    /**
     * Open the settings menu/page
     */
    private void openSettings()
    {
        // TODO: Implement settings page transition
        System.out.println("Settings button clicked!");
        // Greenfoot.setWorld(new SettingsWorld());
    }
}
