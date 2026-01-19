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
    private int tileSize = 48;
    private TiledMap tiledMap;
    private Teacher teacher;
    private TeacherDisplay teacherDisplay;
    private int teacherMapX = 339; // Fixed position on map
    private int teacherMapY = 115;
    private DialogueManager dialogueManager; // For managing dialogue interactions

    public MainMapWorld()
    {
        super(600, 400, 1);
        
        FontManager.loadFonts();
        
        // Initialize dialogue manager
        dialogueManager = DialogueManager.getInstance();
        
        // Load TMX map (floor + collisions)
        loadMap();
        
        // Add teacher NPC to the map (front area) - static on map
        // Added BEFORE character so character renders on top
        teacher = new Teacher();
        teacherDisplay = new TeacherDisplay();
        addObject(teacher, teacherMapX, teacherMapY);
        addObject(teacherDisplay, teacherMapX, teacherMapY);
        
        // Retrieve player data
        String playerName = PlayerData.getPlayerName();
        String playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender - added AFTER teacher to render on top
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
        
        // Add collision objects AFTER character so they render behind
        // (REMOVED: using rectangle-based collision from Collision object layer instead)
        
        // Instructions
        Label instructionsLabel = new Label("Use arrow keys to move", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
    }
    
    private void loadMap()
    {
        try
        {
            tiledMap = new TiledMap("test-map-LayersFixed.tmj");
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            System.out.println("SUCCESS: Loaded TMJ map, backgroundImage size: " + 
                             backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
        }
        catch (Exception e)
        {
            // Fallback: create a simple green background if image not found
            System.out.println("ERROR loading TMJ: " + e.getMessage());
            backgroundImage = new GreenfootImage(getWidth(), getHeight());
            backgroundImage.setColor(new Color(34, 139, 34)); // Forest green
            backgroundImage.fillRect(0, 0, getWidth(), getHeight());
            tiledMap = null;
        }
        
        // Calculate max scroll values to prevent scrolling past the edges
        maxScrollX = Math.max(0, backgroundImage.getWidth() - getWidth());
        maxScrollY = Math.max(0, backgroundImage.getHeight() - getHeight());
    }

    public void act()
    {
        // Process dialogue input (ENTER key to dismiss)
        dialogueManager.processInput();
        
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
            // Fill with solid color first to avoid StartWorld bleed-through
            worldImage.setColor(new Color(0, 0, 0));
            worldImage.fillRect(0, 0, getWidth(), getHeight());
            // Draw map at scroll offset
            if (backgroundImage != null)
            {
                worldImage.drawImage(backgroundImage, -scrollX, -scrollY);
            }
            else
            {
                System.out.println("WARNING: backgroundImage is null!");
            }
            
            // Update teacher position to stay static on map
            updateTeacherPosition();
            
            // Check for world transition
            checkWorldTransition();
        }
    }
    
    /**
     * Update teacher screen position based on scroll to keep it static on map
     */
    private void updateTeacherPosition()
    {
        if (teacher != null && teacher.getWorld() != null)
        {
            int screenX = teacherMapX - scrollX;
            int screenY = teacherMapY - scrollY;
            teacher.setLocation(screenX, screenY);
            teacherDisplay.setLocation(screenX, screenY);
        }
    }
    
    /**
     * Convert screen coordinates to map coordinates
     * Screen coords are relative to the camera view (0-600 width, 0-400 height)
     * Map coords are absolute positions in the full map (0-768 width, 0-576 height)
     */
    public int screenToMapX(int screenX)
    {
        // Update scroll first to ensure latest values
        if (character != null && character.getWorld() != null)
        {
            scrollX = character.getX() - getWidth() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
        }
        return screenX + scrollX;
    }
    
    public int screenToMapY(int screenY)
    {
        // Update scroll first to ensure latest values
        if (character != null && character.getWorld() != null)
        {
            scrollY = character.getY() - getHeight() / 2;
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
        }
        return screenY + scrollY;
    }
    
    /**
     * Check if a rectangle (character's feet area) collides with any collision rectangle
     * @param mapX center X position in map coordinates
     * @param mapY center Y position in map coordinates  
     * @param width width of the feet hitbox
     * @param height height of the feet hitbox (should be small, like 10-15px)
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
    
    /**
     * Check if player should transition to LabWorld
     */
    private void checkWorldTransition()
    {
        if (character == null || backgroundImage == null) return;
        
        // Get character's map position
        int mapX = screenToMapX(character.getX());
        
        // Transition to LabWorld when reaching right edge
        if (mapX >= backgroundImage.getWidth() - 5)
        {
            Greenfoot.setWorld(new LabWorld());
        }
    }
}


