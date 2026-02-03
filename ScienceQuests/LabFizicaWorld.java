import greenfoot.*;
import java.util.*;

public class LabFizicaWorld extends World implements CollisionWorld
{
    private Actor character;
    private PhysicsTeacher teacher;
    private GreenfootImage backgroundImage;
    private GreenfootImage onTopLayerImage;
    private GreenfootImage onTopViewport;
    private OverlayLayer overlayActor;
    private ExperienceBar experienceBar; // XP bar in top-left
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;
    private int tileSize = 48;
    private TiledMap tiledMap;
    private boolean isBroken = false; // Track if lab is in broken state
    private boolean hasTriggeredBreakSequence = false; // Track if initial break happened
    private int frameCounter = 0; // Frame counter for delayed trigger
    private int dialogueWaitCounter = 0; // Counter to wait after dialogue
    private boolean waitingForDialogue = false; // Waiting for dialogue to finish
    
    // Flicker animation state
    private boolean isAnimating = false;
    private int animationPhase = 0;
    private int animationCounter = 0;
    private int flickerCount = 0;
    private boolean showBlack = false;

    public LabFizicaWorld()
    {
        super(864, 672, 1); // 18x14 tiles at 48px
        // Ensure any lingering dialogue state is cleared on world init
        DialogueManager.getInstance().reset();
        
        // Draw UI on top, then overlay, then characters and teacher
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
        setPaintOrder(Label.class, TeacherInteractionDisplay.class, RapidFireQuest.class, KeySequenceQuest.class, AlternatingKeysQuest.class, DoubleTapSprintQuest.class, DirectionDodgeQuest.class, ComboChainQuest.class, RhythmReleaseQuest.class, PrecisionHoldQuest.class, KeyRainfallQuest.class, OverlayLayer.class, Boy.class, Girl.class, PhysicsTeacher.class);
=======
        setPaintOrder(ExperienceBar.class, Label.class, TeacherInteractionDisplay.class, OverlayLayer.class, Boy.class, Girl.class, PhysicsTeacher.class);
>>>>>>> Stashed changes
=======
        setPaintOrder(ExperienceBar.class, Label.class, TeacherInteractionDisplay.class, OverlayLayer.class, Boy.class, Girl.class, PhysicsTeacher.class);
>>>>>>> Stashed changes
=======
        setPaintOrder(ExperienceBar.class, Label.class, TeacherInteractionDisplay.class, OverlayLayer.class, Boy.class, Girl.class, PhysicsTeacher.class);
>>>>>>> Stashed changes
        
        // Load physics lab map
        loadMap("images/labfizica-normal.json");
        
        // Retrieve player data
        Gender playerGender = PlayerData.getPlayerGender();
        
        // Spawn the correct character based on gender at screen center initially
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
        
        // Add physics teacher at MAP coordinates (not screen coordinates)
        // Teacher position in map: x=836, y=266
        int teacherMapX = 836;
        int teacherMapY = 266;
        teacher = new PhysicsTeacher();
        
        // Calculate screen position from map position
        int teacherScreenX = teacherMapX - scrollX;
        int teacherScreenY = teacherMapY - scrollY;
        addObject(teacher, teacherScreenX, teacherScreenY);
        
        DebugLog.log("Teacher added at screen position: (" + teacherScreenX + ", " + teacherScreenY + ")");
        DebugLog.log("Teacher map position: (" + teacherMapX + ", " + teacherMapY + ")");
        DebugLog.log("Current scroll: scrollX=" + scrollX + ", scrollY=" + scrollY);
        
        // Instructions
        Label instructionsLabel = new Label("Apasă F pentru a interacționa", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
        
<<<<<<< Updated upstream
<<<<<<< Updated upstream
<<<<<<< Updated upstream
        // Add mini-quests scattered across the map
        addMiniQuests();
    }
    
    /**
     * Add all mini-quest blocks scattered across the map
     */
    private void addMiniQuests()
    {
        addObject(new RapidFireQuest(100, 100), 100, 100);
        addObject(new KeySequenceQuest(300, 100), 300, 100);
        addObject(new AlternatingKeysQuest(500, 100), 500, 100);
        addObject(new DoubleTapSprintQuest(100, 300), 100, 300);
        addObject(new DirectionDodgeQuest(300, 300), 300, 300);
        addObject(new ComboChainQuest(500, 300), 500, 300);
        addObject(new RhythmReleaseQuest(100, 500), 100, 500);
        addObject(new PrecisionHoldQuest(300, 500), 300, 500);
        addObject(new KeyRainfallQuest(500, 500), 500, 500);
=======
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
>>>>>>> Stashed changes
=======
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
>>>>>>> Stashed changes
=======
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
>>>>>>> Stashed changes
    }
    
    private void loadMap(String mapPath)
    {
        try
        {
            tiledMap = new TiledMap(mapPath);
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            DebugLog.log("SUCCESS: Loaded " + mapPath + ", backgroundImage size: " + 
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
                DebugLog.log("On-Top overlay initialized");
            }
        }
        catch (Exception e)
        {
            DebugLog.log("ERROR loading map: " + e.getMessage());
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
        // Process dialogue input so dialogues can advance/close
        DialogueManager.getInstance().processInput();

        // Trigger break sequence after 60 frames (1 second)
        if (!hasTriggeredBreakSequence)
        {
            frameCounter++;
            if (frameCounter >= 60)
            {
                triggerInitialBreakSequence();
            }
        }
        
        // Handle waiting for dialogue to finish before starting animation
        if (waitingForDialogue)
        {
            // Start flicker once the dialogue is actually closed
            if (!DialogueManager.getInstance().isDialogueActive())
            {
                waitingForDialogue = false;
                dialogueWaitCounter = 0;
                startFlickerAnimation();
            }
        }
        
        // Handle flicker animation
        if (isAnimating)
        {
            updateFlickerAnimation();
            // Continue with normal camera updates even during animation
        }
        
        // Check for G key press to toggle lab state (debug/manual mode)
        if (Greenfoot.isKeyDown("g") && hasTriggeredBreakSequence && !isAnimating && !waitingForDialogue)
        {
            startFlickerAnimation();
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
            
            // Update teacher position based on scroll (teacher at map coords 836, 266)
            if (teacher != null && teacher.getWorld() != null)
            {
                int teacherMapX = 836;
                int teacherMapY = 266;
                int teacherScreenX = teacherMapX - scrollX;
                int teacherScreenY = teacherMapY - scrollY;
                teacher.setLocation(teacherScreenX, teacherScreenY);
            }
            
            // Draw the background (or black during flicker)
            if (!isAnimating || !showBlack)
            {
                drawBackground();
            }
            else
            {
                // Draw black screen during flicker
                GreenfootImage worldImage = getBackground();
                worldImage.setColor(Color.BLACK);
                worldImage.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // Check for transition back to MainMapWorld
            checkWorldTransition();
        }
    }
    
    /**
     * Trigger the initial break sequence when entering the lab
     */
    private void triggerInitialBreakSequence()
    {
        if (hasTriggeredBreakSequence) return;
        hasTriggeredBreakSequence = true;
        
        // Teacher shows panic dialogue
        if (teacher != null)
        {
            teacher.showPanicReaction();
        }
        
        // Start waiting for dialogue to finish (non-blocking)
        waitingForDialogue = true;
        dialogueWaitCounter = 0;
    }
    
    /**
     * Start the flicker animation
     */
    private void startFlickerAnimation()
    {
        isAnimating = true;
        animationPhase = 0;
        animationCounter = 0;
        flickerCount = 0;
        showBlack = false;
    }
    
    /**
     * Update flicker animation (called each frame) - only updates state, rendering done in act()
     */
    private void updateFlickerAnimation()
    {
        animationCounter++;
        
        // Phase 0: Slow flickers (3 times)
        if (animationPhase == 0)
        {
            if (showBlack)
            {
                if (animationCounter >= 4)
                {
                    showBlack = false;
                    animationCounter = 0;
                }
            }
            else
            {
                if (animationCounter >= 8)
                {
                    showBlack = true;
                    animationCounter = 0;
                    flickerCount++;
                    
                    if (flickerCount >= 3)
                    {
                        animationPhase = 1;
                        flickerCount = 0;
                        showBlack = false;
                        animationCounter = 0;
                    }
                }
            }
        }
        // Phase 1: Medium flickers (4 times)
        else if (animationPhase == 1)
        {
            if (showBlack)
            {
                if (animationCounter >= 3)
                {
                    showBlack = false;
                    animationCounter = 0;
                }
            }
            else
            {
                if (animationCounter >= 5)
                {
                    showBlack = true;
                    animationCounter = 0;
                    flickerCount++;
                    
                    if (flickerCount >= 4)
                    {
                        animationPhase = 2;
                        flickerCount = 0;
                        showBlack = false;
                        animationCounter = 0;
                    }
                }
            }
        }
        // Phase 2: Fast flickers (8 times)
        else if (animationPhase == 2)
        {
            if (showBlack)
            {
                if (animationCounter >= 1)
                {
                    showBlack = false;
                    animationCounter = 0;
                }
            }
            else
            {
                if (animationCounter >= 2)
                {
                    showBlack = true;
                    animationCounter = 0;
                    flickerCount++;
                    
                    if (flickerCount >= 8)
                    {
                        animationPhase = 3;
                        animationCounter = 0;
                        showBlack = true;
                    }
                }
            }
        }
        // Phase 3: Final black flash
        else if (animationPhase == 3)
        {
            if (animationCounter >= 5)
            {
                // Animation complete - toggle the map
                finishFlickerAnimation();
            }
        }
    }
    
    /**
     * Finish the flicker animation and toggle map state
     */
    private void finishFlickerAnimation()
    {
        if (isBroken)
        {
            // Switch back to normal
            loadMap("images/labfizica-normal.json");
            isBroken = false;
            DebugLog.log("Lab restored to normal state");
        }
        else
        {
            // Switch to broken
            loadMap("images/labfizica-broken.json");
            isBroken = true;
            DebugLog.log("Lab changed to broken state");
        }
        
        // Draw the new map
        drawBackground();
        
        // Reset animation state
        isAnimating = false;
    }
    
    /**
     * Public method for teacher to repair the lab
     */
    public void repairLab()
    {
        if (isBroken)
        {
            // Start repair animation
            startFlickerAnimation();
        }
    }
    
    /**
     * Check if lab is currently broken
     */
    public boolean isBroken()
    {
        return isBroken;
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
            DebugLog.log("TRANSITION TRIGGERED - Returning to MainMapWorld!");
            WorldNavigator.goToMainMap();
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
