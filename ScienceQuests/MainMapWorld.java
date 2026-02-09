import greenfoot.*;
import java.util.*;

public class MainMapWorld extends World implements CollisionWorld
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
    private SettingsButton settingsButton; // Settings icon in top-right
    private ExperienceBar experienceBar; // XP bar in top-left
    private RapidFireQuest rapidFireQuest;
    private KeySequenceQuest keySequenceQuest;
    private AlternatingKeysQuest alternatingKeysQuest;
    private DoubleTapSprintQuest doubleTapSprintQuest;
    private ComboChainQuest comboChainQuest;
    private DirectionDodgeQuest directionDodgeQuest;
    private DirectionArrow bioArrow;
    private DirectionArrow physArrow;
    private OverlayLayer tutorialOverlay;
    private int tutorialDisplayTicks = 0;

    public MainMapWorld()
    {
        // Match world size to full map size (18x14 tiles at 48px)
        super(864, 672, 1);
        // Ensure any lingering dialogue state is cleared on world init
        DialogueManager.getInstance().reset();
        
        // Set paint order for all quests and UI elements
        // Dialogue boxes on top, then overlays, then UI, then arrows beneath dialogue
        setPaintOrder(DialogueBox.class, OverlayLayer.class, ExperienceBar.class, Label.class, 
                     SettingsButton.class, TeacherDisplay.class, DirectionArrow.class,
                     RapidFireQuest.class, KeySequenceQuest.class, AlternatingKeysQuest.class, 
                     DoubleTapSprintQuest.class, DirectionDodgeQuest.class, ComboChainQuest.class, 
                     RhythmReleaseQuest.class, PrecisionHoldQuest.class, KeyRainfallQuest.class,
                     ChemicalBondQuest.class, DnaReplicationQuest.class, PendulumTimingQuest.class,
                     Boy.class, Girl.class, Teacher.class);
        
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
        Gender playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender - added AFTER teacher to render on top
        if (playerGender == Gender.BOY)
        {
            Boy boy = new Boy();
            addObject(boy, getWidth()/2, getHeight()/2);
            character = boy;
        }
        else if (playerGender == Gender.GIRL)
        {
            Girl girl = new Girl();
            addObject(girl, getWidth()/2, getHeight()/2);
            character = girl;
        }
        // Set character spawn position on the map
        if (character != null)
        {
            character.setLocation(505, 155);
        }
        
        // Add collision objects AFTER character so they render behind
        // (REMOVED: using rectangle-based collision from Collision object layer instead)
        
        // Instructions
        Label instructionsLabel = new Label("Folosește săgeţi pentru a te mișca", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
        
        // Add settings button in top-right corner
        settingsButton = new SettingsButton();
        addObject(settingsButton, getWidth() - 20, 20);
        
        // Add mini-quests scattered across the map
        addMiniQuests();

        // Add direction arrows based on game progression
        // Always show Biology arrow (first lab)
        bioArrow = new DirectionArrow("down", "LAB BIOLOGIE (ÎNCEPE AICI)");
        addObject(bioArrow, 432, 620);
        
        // Only show Physics arrow if Biology lab is completed
        if (GameState.getInstance().isLabCompleted(LabType.BIOLOGY))
        {
            physArrow = new DirectionArrow("left", "LAB FIZICĂ");
            addObject(physArrow, 70, 190);
        }
        
        // Show tutorial popup if first time entering MainMapWorld
        if (!GameState.getInstance().hasShownMainMapTutorial())
        {
            showTutorial();
            GameState.getInstance().setMainMapTutorialShown();
        }
    }
    
    /**
     * Add various mini-quests scattered across the main map world
     */
    private void addMiniQuests()
    {
        // Arcade-style quests scattered around the map
        rapidFireQuest = new RapidFireQuest(150, 200);
        keySequenceQuest = new KeySequenceQuest(400, 180);
        alternatingKeysQuest = new AlternatingKeysQuest(600, 250);
        doubleTapSprintQuest = new DoubleTapSprintQuest(200, 400);
        comboChainQuest = new ComboChainQuest(500, 350);
        directionDodgeQuest = new DirectionDodgeQuest(650, 400);

        addObject(rapidFireQuest, 150, 200);           // Near spawn area
        addObject(keySequenceQuest, 400, 180);         // Right side
        addObject(alternatingKeysQuest, 600, 250);     // Far right
        addObject(doubleTapSprintQuest, 200, 400);     // Bottom left
        addObject(comboChainQuest, 500, 350);          // Center area
        addObject(directionDodgeQuest, 650, 400);      // Bottom right
        
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
    }
    
    private void loadMap()
    {
        try
        {
            tiledMap = new TiledMap("images/classroom-new.json");
            tileSize = tiledMap.tileSize;
            // Render layers in the specified order
            String[] layerOrder = new String[] {
                "Tile Layer 1",
                "Obiecte",
                "Tile Layer 2",
                "Tile Layer 4",
                "Object Layer 1" // collision layer (not rendered if not a tile layer)
            };

            backgroundImage = new GreenfootImage(tiledMap.mapW * tileSize, tiledMap.mapH * tileSize);
            for (String layerName : layerOrder)
            {
                GreenfootImage layerImage = tiledMap.getLayerImage(layerName);
                if (layerImage != null)
                {
                    backgroundImage.drawImage(layerImage, 0, 0);
                }
            }
            DebugLog.log("SUCCESS: Loaded TMJ map, backgroundImage size: " + 
                             backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
        }
        catch (Exception e)
        {
            // Fallback: create a simple green background if image not found
            DebugLog.log("ERROR loading TMJ: " + e.getMessage());
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
        
        // Handle tutorial display
        if (tutorialDisplayTicks > 0)
        {
            tutorialDisplayTicks--;
            if (tutorialDisplayTicks == 0)
            {
                clearTutorial();
            }
            else if (Greenfoot.isKeyDown("enter") || Greenfoot.isKeyDown("space"))
            {
                tutorialDisplayTicks = 0;
                clearTutorial();
            }
        }
        
        // Check if Physics arrow should be added after Biology completion
        if (physArrow == null && GameState.getInstance().isLabCompleted(LabType.BIOLOGY))
        {
            physArrow = new DirectionArrow("left", "LAB FIZICĂ");
            addObject(physArrow, 70, 190);
        }
        
        // Draw full map without scrolling
        if (character != null && character.getWorld() != null)
        {
            scrollX = 0;
            scrollY = 0;
            GreenfootImage worldImage = getBackground();
            worldImage.setColor(new Color(0, 0, 0));
            worldImage.fillRect(0, 0, getWidth(), getHeight());
            if (backgroundImage != null)
            {
                worldImage.drawImage(backgroundImage, 0, 0);
            }
            else
            {
                DebugLog.log("WARNING: backgroundImage is null!");
            }
            updateTeacherPosition();
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
            teacher.setLocation(teacherMapX, teacherMapY);
            teacherDisplay.setLocation(teacherMapX, teacherMapY);
        }
    }
    
    /**
     * Convert screen coordinates to map coordinates
     * Screen coords are relative to the camera view (0-600 width, 0-400 height)
     * Map coords are absolute positions in the full map (0-768 width, 0-576 height)
     */
    public int screenToMapX(int screenX)
    {
        return screenX;
    }
    
    public int screenToMapY(int screenY)
    {
        return screenY;
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
     * Check if player should transition to LabWorld, LabFizicaWorld, or LabBiologyWorld
     */
    private void checkWorldTransition()
    {
        if (character == null || backgroundImage == null) return;

        GameState state = GameState.getInstance();
        boolean canEnterLabs = state.areMainMapQuestsUnlocked() && areMainMapMiniQuestsComplete();
        
        // Get character's map position
        int mapX = screenToMapX(character.getX());
        int mapY = screenToMapY(character.getY());
        
        // Transition to LabWorld (Chemistry) when reaching right edge
        if (mapX >= backgroundImage.getWidth() - 5)
        {
            if (canEnterLabs)
            {
                WorldNavigator.tryEnterLab(LabType.CHEMISTRY);
            }
        }
        
        // Transition to LabFizicaWorld when reaching left edge
        if (mapX <= 5)
        {
            if (canEnterLabs)
            {
                WorldNavigator.tryEnterLab(LabType.PHYSICS);
            }
        }
        
        // Transition to LabBiologyWorld when reaching bottom edge
        if (mapY >= backgroundImage.getHeight() - 5)
        {
            if (canEnterLabs)
            {
                WorldNavigator.tryEnterLab(LabType.BIOLOGY);
            }
        }
    }

    private boolean areMainMapMiniQuestsComplete()
    {
        return rapidFireQuest != null && keySequenceQuest != null && alternatingKeysQuest != null
            && doubleTapSprintQuest != null && comboChainQuest != null && directionDodgeQuest != null
            && rapidFireQuest.isCompleted() && keySequenceQuest.isCompleted()
            && alternatingKeysQuest.isCompleted() && doubleTapSprintQuest.isCompleted()
            && comboChainQuest.isCompleted() && directionDodgeQuest.isCompleted();
    }
    
    private void showTutorial()
    {
        tutorialOverlay = new OverlayLayer();
        addObject(tutorialOverlay, getWidth() / 2, getHeight() / 2);
        
        int panelW = 600;
        int panelH = 350;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        // Background
        img.setColor(new Color(15, 15, 30, 240));
        img.fillRect(0, 0, panelW, panelH);
        
        // Border with glow
        img.setColor(new Color(100, 200, 255, 220));
        img.drawRect(0, 0, panelW - 1, panelH - 1);
        img.setColor(new Color(120, 220, 255, 140));
        img.drawRect(1, 1, panelW - 3, panelH - 3);
        
        // Title
        img.setColor(new Color(255, 220, 100));
        img.setFont(FontManager.getPixeledLarge());
        drawCenteredString(img, "BINE AI VENIT!", panelW / 2, 50);
        
        // Instructions
        img.setFont(FontManager.getPixeled());
        img.setColor(new Color(220, 220, 220));
        
        int startY = 110;
        int lineHeight = 35;
        
        img.drawString("• Obișnuiește-te cu Quiz-urile și mini-quest-urile", 40, startY);
        img.drawString("  din clasă", 40, startY + lineHeight);
        
        img.drawString("• După aceea, mergi la Laboratorul de Biologie", 40, startY + lineHeight * 2);
        img.drawString("  (în jos, la baza clasei) și fă la fel", 40, startY + lineHeight * 3);
        
        img.drawString("• Mergi la Laboratorul de Fizică", 40, startY + lineHeight * 4);
        img.drawString("  (în stânga clasei)", 40, startY + lineHeight * 5);
        
        // Bottom text
        img.setFont(FontManager.getPixeledSmall());
        img.setColor(new Color(100, 200, 255));
        drawCenteredString(img, "Apasă SPACE sau ENTER pentru a continua", panelW / 2, panelH - 30);
        
        tutorialOverlay.setImage(img);
        tutorialDisplayTicks = 600; // Auto-close after 10 seconds if not dismissed
    }
    
    private void clearTutorial()
    {
        if (tutorialOverlay != null && tutorialOverlay.getWorld() != null)
        {
            removeObject(tutorialOverlay);
            tutorialOverlay = null;
        }
    }
    
    private void drawCenteredString(GreenfootImage img, String str, int centerX, int y)
    {
        int strWidth = img.getFont().getSize() * str.length() / 2;
        img.drawString(str, centerX - strWidth / 2, y);
    }
}
