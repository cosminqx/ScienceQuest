import greenfoot.*;
import java.util.*;

public class MainMapWorld extends World
{
    private Actor character;
    private GreenfootImage backgroundImage;
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;
    private List<CollisionObject> collisionObjects = new ArrayList<>();
    private int tileSize = 48;
    private TiledMap tiledMap;

    public MainMapWorld()
    {
        super(600, 400, 1);
        
        // Load TMX map (floor + collisions)
        loadMap();
        
        // Retrieve player data
        String playerName = PlayerData.getPlayerName();
        String playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender FIRST
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
        
        // Add collision objects AFTER character so they render behind
        addCollisionObjects();
        
        // Instructions
        Label instructionsLabel = new Label("Use arrow keys to move", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
    
    private void loadMap()
    {
        try
        {
            tiledMap = new TiledMap("test-map.tmx");
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
        }
        catch (Exception e)
        {
            // Fallback: create a simple green background if image not found
            backgroundImage = new GreenfootImage(getWidth(), getHeight());
            backgroundImage.setColor(new Color(34, 139, 34)); // Forest green
            backgroundImage.fillRect(0, 0, getWidth(), getHeight());
            tiledMap = null;
        }
        
        // Calculate max scroll values to prevent scrolling past the edges
        maxScrollX = Math.max(0, backgroundImage.getWidth() - getWidth());
        maxScrollY = Math.max(0, backgroundImage.getHeight() - getHeight());
    }

    private void addCollisionObjects()
    {
        if (tiledMap == null) return;

        for (int y = 0; y < tiledMap.mapH; y++)
        {
            for (int x = 0; x < tiledMap.mapW; x++)
            {
                boolean isSolid = tiledMap.solid[y][x];
                if (!isSolid) continue;

                int pixelX = x * tileSize + tileSize / 2;
                int pixelY = y * tileSize + tileSize / 2;

                // Use Wall for primary collision layer, Desk for secondary (visual differentiation)
                Actor collider;
                if (tiledMap.collisionA[y][x] != 0)
                {
                    collider = new Wall();
                }
                else
                {
                    collider = new Desk();
                }

                makeColliderInvisible(collider);
                collisionObjects.add(new CollisionObject(collider, pixelX, pixelY));
            }
        }

        updateCollisionObjectPositions();
    }

    private void makeColliderInvisible(Actor collider)
    {
        GreenfootImage img = collider.getImage();
        if (img == null)
        {
            img = new GreenfootImage(tileSize, tileSize);
        }
        else
        {
            img = new GreenfootImage(img); // clone to avoid shared refs
        }
        img.setTransparency(0);
        collider.setImage(img);
    }
    
    private void updateCollisionObjectPositions()
    {
        // Update scroll values first if character exists
        if (character != null && character.getWorld() != null)
        {
            scrollX = character.getX() - getWidth() / 2;
            scrollY = character.getY() - getHeight() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        }
        
        for (CollisionObject obj : collisionObjects)
        {
            if (obj.actor.getWorld() == null)
            {
                // Calculate screen position from map position
                int screenX = obj.mapX - scrollX;
                int screenY = obj.mapY - scrollY;
                addObject(obj.actor, screenX, screenY);
                System.out.println("Added desk at screen position: " + screenX + ", " + screenY + " (map: " + obj.mapX + ", " + obj.mapY + ")");
            }
        }
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
            
            // Update collision object screen positions to match scroll
            for (CollisionObject obj : collisionObjects)
            {
                int screenX = obj.mapX - scrollX;
                int screenY = obj.mapY - scrollY;
                
                // Always update position to match scroll
                obj.actor.setLocation(screenX, screenY);
            }
        }
    }
    
    /**
     * Get the background image dimensions for collision checking
     */
    public GreenfootImage getBackgroundImage()
    {
        return backgroundImage;
    }
    
    /**
     * Get the current scroll offset for proper collision detection
     */
    public int getScrollX()
    {
        return scrollX;
    }
    
    public int getScrollY()
    {
        return scrollY;
    }
    
    /**
     * Inner class to store collision object data
     */
    private class CollisionObject
    {
        Actor actor;
        int mapX;
        int mapY;
        
        CollisionObject(Actor actor, int mapX, int mapY)
        {
            this.actor = actor;
            this.mapX = mapX;
            this.mapY = mapY;
        }
    }
}


