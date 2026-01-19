import greenfoot.*;
import java.util.*;

public class LabWorld extends World
{
    private Actor character;
    private GreenfootImage backgroundImage;
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;
    private int tileSize = 48;
    private TiledMap tiledMap;

    public LabWorld()
    {
        super(600, 400, 1);
        
        // Load lab map FIRST
        loadMap();
        
        // Retrieve player data
        String playerName = PlayerData.getPlayerName();
        String playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender at screen center initially
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
        
        // Now set character to map position 74, 163 (will be adjusted by scroll in act())
        if (character != null)
        {
            character.setLocation(74, 163);
            // Force initial scroll calculation
            scrollX = character.getX() - getWidth() / 2;
            scrollY = character.getY() - getHeight() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            System.out.println("Character spawned at: " + character.getX() + ", " + character.getY());
            System.out.println("Initial scroll: " + scrollX + ", " + scrollY);
        }
        
        // Instructions
        Label instructionsLabel = new Label("Use arrow keys to move", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
    
    private void loadMap()
    {
        try
        {
            // Load the lab map
            tiledMap = new TiledMap("images/lab_noapte_2.json");
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            System.out.println("SUCCESS: Loaded lab map, backgroundImage size: " + 
                             backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
        }
        catch (Exception e)
        {
            // Fallback: create a simple background if image not found
            System.out.println("ERROR loading lab map: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = new GreenfootImage(getWidth(), getHeight());
            backgroundImage.setColor(new Color(34, 34, 50)); // Dark blue-gray
            backgroundImage.fillRect(0, 0, getWidth(), getHeight());
            tiledMap = null;
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
            worldImage.setColor(new Color(0, 0, 0));
            worldImage.fillRect(0, 0, getWidth(), getHeight());
            
            if (backgroundImage != null)
            {
                worldImage.drawImage(backgroundImage, -scrollX, -scrollY);
            }
            else
            {
                System.out.println("WARNING: backgroundImage is null!");
            }
            
            // Check for transition back to MainMapWorld
            checkWorldTransition();
        }
    }
    
    /**
     * Check if player should transition back to MainMapWorld
     */
    private void checkWorldTransition()
    {
        if (character == null) return;
        
        // Get character's map position
        int mapX = screenToMapX(character.getX());
        
        // Transition to MainMapWorld when reaching left edge
        if (mapX <= 5)
        {
            Greenfoot.setWorld(new MainMapWorld());
        }
    }
    
    /**
     * Convert screen coordinates to map coordinates
     */
    public int screenToMapX(int screenX)
    {
        if (character != null && character.getWorld() != null)
        {
            scrollX = character.getX() - getWidth() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
        }
        return screenX + scrollX;
    }
    
    public int screenToMapY(int screenY)
    {
        if (character != null && character.getWorld() != null)
        {
            scrollY = character.getY() - getHeight() / 2;
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        }
        return screenY + scrollY;
    }
    
    /**
     * Check if a rectangle (character's feet area) collides with any collision rectangle
     */
    public boolean isCollisionAt(int mapX, int mapY, int width, int height)
    {
        if (tiledMap == null) return false;
        
        // Calculate feet rectangle bounds (centered on mapX, mapY)
        int x1 = mapX - width / 2;
        int y1 = mapY - height / 2;
        int x2 = x1 + width;
        int y2 = y1 + height;
        
        // AABB collision check against all collision rectangles
        for (TiledMap.CollisionRect r : tiledMap.collisionRects)
        {
            if (x1 < r.x + r.w && x2 > r.x &&
                y1 < r.y + r.h && y2 > r.y)
            {
                return true;
            }
        }
        return false;
    }
}
