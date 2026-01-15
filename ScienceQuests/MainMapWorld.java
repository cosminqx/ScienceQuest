import greenfoot.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

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

    public MainMapWorld()
    {
        super(600, 400, 1);
        
        // Load and set up the background image
        setUpBackground();
        
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
        
        // Load map data from JSON and add collision objects AFTER character
        loadMapFromJson();
        
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
    
    private void loadMapFromJson()
    {
        try
        {
            // Read map.json file from parent directory
            String jsonPath = "../map.json";
            String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
            
            // Parse collision layer manually
            parseCollisionLayer(content);
            
            // Add all collision objects to the world
            updateCollisionObjectPositions();
        }
        catch (Exception e)
        {
            System.out.println("Could not load map.json: " + e.getMessage());
        }
    }
    
    private void parseCollisionLayer(String json)
    {
        // Find the Collision layer
        int collisionStart = json.indexOf("\"name\":\"Collision\"");
        if (collisionStart == -1) return;
        
        // Find tiles array within collision layer
        int tilesStart = json.indexOf("\"tiles\":[", collisionStart);
        if (tilesStart == -1) return;
        
        int tilesEnd = json.indexOf("]", tilesStart);
        String tilesSection = json.substring(tilesStart + 9, tilesEnd);
        
        // Parse each tile object
        String[] tiles = tilesSection.split("\\},\\{");
        for (String tile : tiles)
        {
            tile = tile.replace("{", "").replace("}", "");
            
            int x = -1, y = -1;
            
            // Extract x coordinate
            int xPos = tile.indexOf("\"x\":");
            if (xPos != -1)
            {
                int xStart = xPos + 4;
                int xEnd = tile.indexOf(",", xStart);
                if (xEnd == -1) xEnd = tile.length();
                try {
                    x = Integer.parseInt(tile.substring(xStart, xEnd).trim());
                } catch (Exception e) {}
            }
            
            // Extract y coordinate
            int yPos = tile.indexOf("\"y\":");
            if (yPos != -1)
            {
                int yStart = yPos + 4;
                int yEnd = tile.indexOf(",", yStart);
                if (yEnd == -1) yEnd = tile.length();
                try {
                    y = Integer.parseInt(tile.substring(yStart, yEnd).trim());
                } catch (Exception e) {}
            }
            
            // Add desk at tile position
            if (x >= 0 && y >= 0)
            {
                int pixelX = x * tileSize + tileSize / 2;
                int pixelY = y * tileSize + tileSize / 2;
                collisionObjects.add(new CollisionObject(new Desk(), pixelX, pixelY));
            }
        }
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


