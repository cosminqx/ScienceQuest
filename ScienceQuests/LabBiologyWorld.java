import greenfoot.*;
import java.util.*;

public class LabBiologyWorld extends World implements CollisionWorld
{
    private Actor character;
    private BiologyTeacher teacher;
    private BiologyAssistant assistant;
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
    private boolean isDestroyed = false; // Track if lab is in destroyed state
    private boolean hasTriggeredDestroySequence = false; // Track if initial destruction happened
    private int frameCounter = 0; // Frame counter for delayed trigger
    private int dialogueWaitCounter = 0; // Counter to wait after dialogue
    private boolean waitingForDialogue = false; // Waiting for dialogue to finish
    private boolean miniQuestsAdded = false;
    private boolean repairTriggered = false;
    private DnaReplicationQuest dnaQuest;
    private ChemicalBondQuest bondQuest;
    private PrecisionHoldQuest precisionQuest;
    
    // Flicker animation state
    private boolean isAnimating = false;
    private int animationPhase = 0;
    private int animationCounter = 0;
    private int flickerCount = 0;
    private boolean showBlack = false;

    public LabBiologyWorld()
    {
        super(864, 672, 1); // 18x14 tiles at 48px
        // Ensure any lingering dialogue state is cleared on world init
        DialogueManager.getInstance().reset();
        
        // Draw UI on top, then overlay, then arrows, then characters and teacher
        setPaintOrder(DialogueBox.class, OverlayLayer.class, ExperienceBar.class, Label.class, 
                     TeacherInteractionDisplay.class, DirectionArrow.class, DnaReplicationQuest.class, 
                     ChemicalBondQuest.class, PrecisionHoldQuest.class, Boy.class, Girl.class, 
                     BiologyTeacher.class, BiologyAssistant.class);
        
        // Load biology lab map (start destroyed until repaired, unless already completed)
        if (GameState.getInstance().isLabCompleted(LabType.BIOLOGY))
        {
            loadMap("images/LabBiologyWorld-Normal.json");
            isDestroyed = false;
            hasTriggeredDestroySequence = true;
        }
        else
        {
            loadMap("images/LabBiologyWorld-destroyed.json");
            isDestroyed = true;
            hasTriggeredDestroySequence = true;
        }
        
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
            // Spawn at top of the map (entering from MainMapWorld bottom wall)
            // Map is 672px tall, spawn at y=50 in map coords, center horizontally
            int targetMapX = backgroundImage.getWidth() / 2;  // Center of map width
            int targetMapY = 50;  // Near top edge of map
            
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
        
        // Add biology teacher at MAP coordinates
        // Teacher position in map: x=144, y=266
        int teacherMapX = 144;
        int teacherMapY = 266;
        teacher = new BiologyTeacher();
        
        // Calculate screen position from map position
        int teacherScreenX = teacherMapX - scrollX;
        int teacherScreenY = teacherMapY - scrollY;
        addObject(teacher, teacherScreenX, teacherScreenY);
        
        DebugLog.log("Biology Teacher added at screen position: (" + teacherScreenX + ", " + teacherScreenY + ")");
        DebugLog.log("Biology Teacher map position: (" + teacherMapX + ", " + teacherMapY + ")");
        DebugLog.log("Current scroll: scrollX=" + scrollX + ", scrollY=" + scrollY);
        
        // Add biology assistant at MAP coordinates (opposite side of the lab)
        // Assistant position in map: x=720, y=266
        int assistantMapX = 720;
        int assistantMapY = 266;
        assistant = new BiologyAssistant();
        
        // Calculate screen position from map position
        int assistantScreenX = assistantMapX - scrollX;
        int assistantScreenY = assistantMapY - scrollY;
        addObject(assistant, assistantScreenX, assistantScreenY);
        
        DebugLog.log("Biology Assistant added at screen position: (" + assistantScreenX + ", " + assistantScreenY + ")");
        DebugLog.log("Biology Assistant map position: (" + assistantMapX + ", " + assistantMapY + ")");
        
        // Instructions
        Label instructionsLabel = new Label("Apasă F pentru a interacționa", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
        
        // Add mini-quests only after NPC quiz gate is completed
        if (GameState.getInstance().isLabBioQuizGateComplete())
        {
            addMiniQuests();
            miniQuestsAdded = true;
        }
        
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
        
        // Add return arrow at top to go back to MainMapWorld (only if lab is completed)
        if (GameState.getInstance().isLabCompleted(LabType.BIOLOGY))
        {
            addObject(new DirectionArrow("up", "ÎNAPOI LA CLASĂ"), getWidth() / 2, 60);
        }
    }
    
    /**
     * Add biology-specific mini-quests to the lab
     */
    private void addMiniQuests()
    {
        // Different biology-themed challenges at specified positions
        dnaQuest = new DnaReplicationQuest(77, 547);
        bondQuest = new ChemicalBondQuest(222, 547);
        precisionQuest = new PrecisionHoldQuest(357, 547);

        addObject(dnaQuest, 77, 547);           // DNA base pairing
        addObject(bondQuest, 222, 547);         // Molecular bonding
        addObject(precisionQuest, 357, 547);    // Precision timing
    }

    private boolean areLabMiniQuestsComplete()
    {
        return dnaQuest != null && bondQuest != null && precisionQuest != null
            && dnaQuest.isCompleted() && bondQuest.isCompleted() && precisionQuest.isCompleted();
    }
    
    private void loadMap(String mapPath)
    {
        try
        {
            DebugLog.log("====== Loading Biology Lab Map: " + mapPath + " ======");
            tiledMap = new TiledMap(mapPath);
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            
            // Calculate max scroll
            maxScrollX = Math.max(0, backgroundImage.getWidth() - getWidth());
            maxScrollY = Math.max(0, backgroundImage.getHeight() - getHeight());
            
            DebugLog.log("Loaded biology lab map: " + backgroundImage.getWidth() + "x" + backgroundImage.getHeight());
            DebugLog.log("Max scroll: " + maxScrollX + ", " + maxScrollY);
            DebugLog.log("====== Biology Lab Map Loading Complete ======");
            
            // Check for "On-Top" layer
            onTopLayerImage = tiledMap.getLayerImage("On-Top");
            if (onTopLayerImage != null)
            {
                DebugLog.log("Found On-Top layer: " + onTopLayerImage.getWidth() + "x" + onTopLayerImage.getHeight());
                onTopViewport = new GreenfootImage(getWidth(), getHeight());
                
                // Add overlay actor if not present
                if (overlayActor == null)
                {
                    overlayActor = new OverlayLayer();
                    addObject(overlayActor, getWidth()/2, getHeight()/2);
                }
                // Update the viewport image that will be set in drawBackground
                overlayActor.setImage(onTopViewport);
            }
        }
        catch (Exception e)
        {
            DebugLog.log("Error loading biology lab map: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void act()
    {
        // Process dialogue input so dialogues can advance/close
        DialogueManager.getInstance().processInput();

        // No automatic destroy sequence; lab starts destroyed until repaired
        
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
        if (Greenfoot.isKeyDown("g") && hasTriggeredDestroySequence && !isAnimating && !waitingForDialogue)
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
            
            // Update teacher position based on scroll (teacher at map coords 144, 266)
            if (teacher != null && teacher.getWorld() != null)
            {
                int teacherMapX = 144;
                int teacherMapY = 266;
                int teacherScreenX = teacherMapX - scrollX;
                int teacherScreenY = teacherMapY - scrollY;
                teacher.setLocation(teacherScreenX, teacherScreenY);
            }
            
            // Update assistant position based on scroll (assistant at map coords 720, 266)
            if (assistant != null && assistant.getWorld() != null)
            {
                int assistantMapX = 720;
                int assistantMapY = 266;
                int assistantScreenX = assistantMapX - scrollX;
                int assistantScreenY = assistantMapY - scrollY;
                assistant.setLocation(assistantScreenX, assistantScreenY);
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
        }
        
        // Gate mini-quests until NPC quiz requirement is met
        if (!miniQuestsAdded && GameState.getInstance().isLabBioQuizGateComplete())
        {
            addMiniQuests();
            miniQuestsAdded = true;
        }

        // Repair lab after mini-quests are completed
        if (!repairTriggered && isDestroyed && miniQuestsAdded && areLabMiniQuestsComplete())
        {
            repairTriggered = true;
            repairLab();
            GameState state = GameState.getInstance();
            if (!state.isLabCompleted(LabType.BIOLOGY))
            {
                state.completeLab(LabType.BIOLOGY);
                state.awardBadge("biology_master");
                state.addXp(50);
            }
        }

        // Check for world transitions
        checkWorldTransition();
    }
    
    /**
     * Trigger the initial lab destruction sequence (happens once)
     */
    private void triggerInitialDestroySequence()
    {
        if (hasTriggeredDestroySequence) return;
        hasTriggeredDestroySequence = true;
        
        DebugLog.log("Triggering biology lab destruction sequence...");
        
        // Show panic dialogue through teacher
        if (teacher != null)
        {
            teacher.showPanicReaction();
        }
        
        // Wait for dialogue to finish, then start animation
        waitingForDialogue = true;
        dialogueWaitCounter = 0;
    }
    
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
        if (isDestroyed)
        {
            // Switch back to normal
            loadMap("images/LabBiologyWorld-Normal.json");
            isDestroyed = false;
            DebugLog.log("Biology lab restored to normal state");
        }
        else
        {
            // Switch to destroyed
            loadMap("images/LabBiologyWorld-destroyed.json");
            isDestroyed = true;
            DebugLog.log("Biology lab changed to destroyed state");
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
        if (isDestroyed)
        {
            // Start repair animation
            startFlickerAnimation();
        }
    }
    
    /**
     * Check if lab is currently destroyed
     */
    public boolean isDestroyed()
    {
        return isDestroyed;
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
        if (onTopLayerImage != null && onTopViewport != null && overlayActor != null)
        {
            onTopViewport.clear();
            onTopViewport.drawImage(onTopLayerImage, -scrollX, -scrollY);
            overlayActor.setImage(onTopViewport);
            overlayActor.setLocation(getWidth() / 2, getHeight() / 2);
        }
    }
    
    /**
     * Check if player should transition back to MainMapWorld (exit through top wall)
     */
    private void checkWorldTransition()
    {
        if (character == null) return;
        
        // Get character's map position
        int mapX = screenToMapX(character.getX());
        int mapY = screenToMapY(character.getY());
        
        // Transition to MainMapWorld when reaching top edge
        if (mapY <= 5)
        {
            DebugLog.log("TRANSITION TRIGGERED - Returning to MainMapWorld from Biology Lab!");
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
