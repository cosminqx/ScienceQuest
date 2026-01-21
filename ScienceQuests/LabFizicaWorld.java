import greenfoot.*;
import java.util.*;

public class LabFizicaWorld extends World
{
    private Actor character;
    private GreenfootImage backgroundImage;
    private GreenfootImage onTopLayerImage;
    private GreenfootImage onTopViewport;
    private OverlayLayer overlayActor;
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;
    private int tileSize = 48;
    private TiledMap tiledMap;
    private boolean isBroken = false; // Track if lab is in broken state

    public LabFizicaWorld()
    {
        super(864, 672, 1); // 18x14 tiles at 48px
        
        // Draw UI on top, then overlay, then characters
        setPaintOrder(Label.class, OverlayLayer.class, Boy.class, Girl.class);
        
        // Load physics lab map
        loadMap("images/labfizica-normal.json");
        
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
        
        // Set character spawn position 
        if (character != null)
        {
            // Spawn at right side of the map (entering from MainMapWorld left wall)
            // Map is 864px wide, spawn at x=780 in map coords, center vertically
            int targetMapX = 780;  // Near right edge of map
            int targetMapY = backgroundImage.getHeight() / 2;  // Center of map height
            
            // Calculate screen position from map position
            // We want to center the view on the character
            scrollX = targetMapX - getWidth() / 2;
            scrollY = targetMapY - getHeight() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            // Set character at center of screen (scroll will handle the offset)
            int screenX = targetMapX - scrollX;
            int screenY = targetMapY - scrollY;
            character.setLocation(screenX, screenY);
        }
        
        // Draw initial background
        drawBackground();
        
        // Instructions
        Label instructionsLabel = new Label("Press G to break/fix the lab", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
    
    private void loadMap(String mapPath)
    {
        try
        {
            tiledMap = new TiledMap(mapPath);
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            System.out.println("SUCCESS: Loaded " + mapPath + ", backgroundImage size: " + 
                             backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
            
            // Prepare optional overlay layer that should draw above the player
            onTopLayerImage = tiledMap.getLayerImage("On-Top");
            if (onTopLayerImage != null)
            {
                onTopViewport = new GreenfootImage(getWidth(), getHeight());
                if (overlayActor == null)
                {
                    overlayActor = new OverlayLayer();
                    overlayActor.setImage(onTopViewport);
                    addObject(overlayActor, getWidth() / 2, getHeight() / 2);
                }
                System.out.println("On-Top overlay initialized");
            }
        }
        catch (Exception e)
        {
            System.out.println("ERROR loading map: " + e.getMessage());
            e.printStackTrace();
            backgroundImage = new GreenfootImage(getWidth(), getHeight());
            backgroundImage.setColor(new Color(34, 34, 50));
            backgroundImage.fillRect(0, 0, getWidth(), getHeight());
            tiledMap = null;
        }
        
        // Calculate max scroll values
        maxScrollX = Math.max(0, backgroundImage.getWidth() - getWidth());
        maxScrollY = Math.max(0, backgroundImage.getHeight() - getHeight());
    }

    public void act()
    {
        // Check for G key press to toggle lab state
        if (Greenfoot.isKeyDown("g"))
        {
            toggleLabState();
            Greenfoot.delay(10); // Prevent rapid toggling
        }
        
        // Update the camera position to keep the character centered
        if (character != null && character.getWorld() != null)
        {
            // Calculate scroll position to center the character
            scrollX = character.getX() - getWidth() / 2;
            scrollY = character.getY() - getHeight() / 2;
            
            // Clamp scroll values
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            // Draw the background
            drawBackground();
            
            // Check for transition back to MainMapWorld
            checkWorldTransition();
        }
    }
    
    /**
     * Draw the background and overlay layers
     */
    private void drawBackground()
    {
        // Draw the background image with scroll offset
        GreenfootImage worldImage = getBackground();
        worldImage.setColor(new Color(0, 0, 0));
        worldImage.fillRect(0, 0, getWidth(), getHeight());
        
        if (backgroundImage != null)
        {
            worldImage.drawImage(backgroundImage, -scrollX, -scrollY);
        }
        
        // Update overlay if present
        if (onTopLayerImage != null && onTopViewport != null)
        {
            onTopViewport.setColor(new Color(0, 0, 0, 0));
            onTopViewport.fillRect(0, 0, getWidth(), getHeight());
            onTopViewport.drawImage(onTopLayerImage, -scrollX, -scrollY);
        }
    }
    
    /**
     * Toggle between normal and broken lab states
     */
    private void toggleLabState()
    {
        if (isBroken)
        {
            // Switch back to normal
            loadMap("images/labfizica-normal.json");
            isBroken = false;
            System.out.println("Lab restored to normal state");
        }
        else
        {
            // Switch to broken
            loadMap("images/labfizica-broken.json");
            isBroken = true;
            System.out.println("Lab changed to broken state");
        }
    }
    
    /**
     * Check if player should transition back to MainMapWorld (exit through left wall)
     */
    private void checkWorldTransition()
    {
        if (character == null) return;
        
        // Get character's map position
        int mapX = screenToMapX(character.getX());
        int mapY = screenToMapY(character.getY());
        
        // Transition to MainMapWorld when reaching left edge
        if (mapX <= 5)
        {
            System.out.println("TRANSITION TRIGGERED - Returning to MainMapWorld!");
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
        
        int x1 = mapX - width / 2;
        int y1 = mapY - height / 2;
        int x2 = x1 + width;
        int y2 = y1 + height;
        
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
