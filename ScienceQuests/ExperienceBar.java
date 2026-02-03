import greenfoot.*;

/**
 * ExperienceBar - Displays a green XP bar in the top-left corner of the screen
 * Updates in real-time based on the player's current XP
 */
public class ExperienceBar extends Actor
{
    private static final int BAR_WIDTH = 200;
    private static final int BAR_HEIGHT = 20;
    private static final int LEVEL_TEXT_HEIGHT = 20;
    private static final int TOTAL_HEIGHT = BAR_HEIGHT + LEVEL_TEXT_HEIGHT;
    private static final int BORDER_WIDTH = 2;
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Color BG_COLOR = new Color(40, 40, 40);
    private static final Color FILL_COLOR = new Color(50, 200, 50); // Green
    
    private int lastXP = -1; // Track last XP to avoid unnecessary redraws
    private int lastLevel = -1; // Track last level to detect level-ups
    
    public ExperienceBar()
    {
        updateImage();
    }
    
    public void act()
    {
        // Get current XP and level from GameState
        GameState state = GameState.getInstance();
        int currentXP = state.getXp();
        int currentLevel = state.getLevel();
        
        // Only redraw if XP or level changed
        if (currentXP != lastXP || currentLevel != lastLevel)
        {
            lastXP = currentXP;
            lastLevel = currentLevel;
            updateImage();
        }
    }
    
    /**
     * Redraw the XP bar based on current XP percentage
     * Displays level text in top-left corner of bar and "Level X" below the bar
     */
    private void updateImage()
    {
        GameState state = GameState.getInstance();
        float xpPercent = state.getXPPercent();
        int level = state.getLevel();
        
        GreenfootImage img = new GreenfootImage(BAR_WIDTH, TOTAL_HEIGHT);
        
        // Draw background
        img.setColor(BG_COLOR);
        img.fillRect(0, 0, BAR_WIDTH, BAR_HEIGHT);
        
        // Draw green fill based on XP percentage
        int fillWidth = (int)((BAR_WIDTH - BORDER_WIDTH * 2) * xpPercent);
        img.setColor(FILL_COLOR);
        img.fillRect(BORDER_WIDTH, BORDER_WIDTH, fillWidth, BAR_HEIGHT - BORDER_WIDTH * 2);
        
        // Draw border
        img.setColor(BORDER_COLOR);
        img.drawRect(0, 0, BAR_WIDTH - 1, BAR_HEIGHT - 1);
        img.drawRect(1, 1, BAR_WIDTH - 3, BAR_HEIGHT - 3);
        
        // Draw level text in top-left corner of bar
        img.setFont(FontManager.getPixeledSmall());
        img.setColor(Color.WHITE);
        String levelText = "LVL " + level;
        img.drawString(levelText, 4, 10);
        
        // Draw XP text in center
        String xpText = state.getXp() + "/" + state.getMaxXP();
        int textX = (BAR_WIDTH - (xpText.length() * 6)) / 2;
        img.drawString(xpText, textX, BAR_HEIGHT - 5);
        
        // Draw "Level X" text below the bar
        img.setFont(FontManager.getPixeled());
        img.setColor(Color.WHITE);
        String levelDisplayText = "Level " + level;
        int levelDisplayX = (BAR_WIDTH - (levelDisplayText.length() * 8)) / 2;
        img.drawString(levelDisplayText, levelDisplayX, BAR_HEIGHT + 16);
        
        setImage(img);
    }
}
