import greenfoot.*;
import java.util.*;

public class LabWorld extends World implements CollisionWorld
{
    private Actor character;
    private ChemistryTeacher teacher;
    private GreenfootImage backgroundImage;
    private GreenfootImage overPlayerLayerImage;
    private GreenfootImage overPlayerViewport;
    private OverlayLayer overlayActor;
    private ExperienceBar experienceBar; // XP bar in top-left
    private int scrollX = 0;
    private int scrollY = 0;
    private int maxScrollX;
    private int maxScrollY;
    private int tileSize = 48;
    private TiledMap tiledMap;
    private List<QuestBlock> questBlocks;
    private boolean miniQuestsAdded = false;
    
    // Chemistry-specific mini-quests
    private KeyRainfallQuest rainfallQuest;
    private ComboChainQuest comboQuest;
    private RhythmReleaseQuest rhythmQuest;

    public LabWorld()
    {
        super(800, 600, 1);

        // Draw UI on top, then overlay, then characters
        setPaintOrder(DialogueBox.class, OverlayLayer.class, ExperienceBar.class, Label.class, TimingQuestUI.class, QuestBlock.class, RapidFireQuest.class, KeySequenceQuest.class, AlternatingKeysQuest.class, DoubleTapSprintQuest.class, DirectionDodgeQuest.class, ComboChainQuest.class, RhythmReleaseQuest.class, PrecisionHoldQuest.class, KeyRainfallQuest.class, Boy.class, Girl.class, ChemistryTeacher.class);
        
        // Ensure any lingering dialogue state is cleared on world init
        DialogueManager.getInstance().reset();
        
        // Initialize quest blocks list
        questBlocks = new ArrayList<QuestBlock>();
        
        // Load lab map FIRST
        loadMap();
        
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
        
        // Now set character to map position 74, 163 (will be adjusted by scroll in act())
        if (character != null)
        {
            character.setLocation(74, 163);
            // Force initial scroll calculation
            scrollX = character.getX() - getWidth() / 2;
            scrollY = character.getY() - getHeight() / 2;
            scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
            scrollY = Math.max(0, Math.min(scrollY, maxScrollY));
            
            DebugLog.log("Character spawned at: " + character.getX() + ", " + character.getY());
            DebugLog.log("Initial scroll: " + scrollX + ", " + scrollY);
        }
        
        // Add Chemistry teacher near the character spawn for easy interaction
        teacher = new ChemistryTeacher();
        addObject(teacher, 250, 200);
        DebugLog.log("Chemistry Teacher added to LabWorld at (250, 200)");
        
        // Instructions
        Label instructionsLabel = new Label("Apasă F pentru a interacționa", 16, Color.WHITE);
        addObject(instructionsLabel, getWidth()/2, getHeight() - 30);
        
        // Add XP bar in top-left corner
        experienceBar = new ExperienceBar();
        addObject(experienceBar, 110, 20);
        
        // Add mini-quests only after NPC quiz gate is completed
        if (GameState.getInstance().isLabChemQuizGateComplete())
        {
            addMiniQuests();
            miniQuestsAdded = true;
        }
        
        // Add return arrow at top to go back to MainMapWorld (only if lab is completed)
        if (GameState.getInstance().isLabCompleted(LabType.CHEMISTRY))
        {
            addObject(new DirectionArrow("up", "ÎNAPOI LA CLASĂ"), getWidth() / 2, 60);
        }
    }
    
    /**
     * Add chemistry-specific mini-quests to the lab
     */
    private void addMiniQuests()
    {
        // Three chemistry-themed challenges positioned below the main area
        rainfallQuest = new KeyRainfallQuest(150, 400);
        comboQuest = new ComboChainQuest(300, 400);
        rhythmQuest = new RhythmReleaseQuest(450, 400);

        addObject(rainfallQuest, 150, 400);      // Chemical reactions (elements falling)
        addObject(comboQuest, 300, 400);         // Chain reactions
        addObject(rhythmQuest, 450, 400);        // Periodic table patterns
        
        DebugLog.log("Chemistry mini-quests added to lab");
    }
    
    private boolean areLabMiniQuestsComplete()
    {
        return rainfallQuest != null && comboQuest != null && rhythmQuest != null
            && rainfallQuest.isCompleted() && comboQuest.isCompleted() && rhythmQuest.isCompleted();
    }
    
    private void loadMap()
    {
        try
        {
            // Load the lab map
            tiledMap = new TiledMap("images/lab_noapte_2.json");
            tileSize = tiledMap.tileSize;
            backgroundImage = tiledMap.getFullMapImage();
            DebugLog.log("SUCCESS: Loaded lab map, backgroundImage size: " + 
                             backgroundImage.getWidth() + "x" + backgroundImage.getHeight());

            // Prepare optional overlay layer that should draw above the player
            overPlayerLayerImage = tiledMap.getLayerImage("Over-Player");
            if (overPlayerLayerImage != null)
            {
                overPlayerViewport = new GreenfootImage(getWidth(), getHeight());
                overlayActor = new OverlayLayer();
                overlayActor.setImage(overPlayerViewport);
                addObject(overlayActor, getWidth() / 2, getHeight() / 2);
                DebugLog.log("Over-Player overlay initialized");
            }
        }
        catch (Exception e)
        {
            // Fallback: create a simple background if image not found
            DebugLog.log("ERROR loading lab map: " + e.getMessage());
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
        // Process dialogue input so dialogues can advance/close
        DialogueManager.getInstance().processInput();

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
                DebugLog.log("WARNING: backgroundImage is null!");
            }

            updateOverlayImage();
            
            // Update quest block positions
            updateQuestBlockPositions();
            
            // Check if player got enough correct quizzes to unlock mini-quests
            if (!miniQuestsAdded && GameState.getInstance().isLabChemQuizGateComplete())
            {
                addMiniQuests();
                miniQuestsAdded = true;
                DebugLog.log("Chemistry mini-quests unlocked!");
            }
            
            // Check if all mini-quests are completed to mark lab as complete
            if (miniQuestsAdded && areLabMiniQuestsComplete() && !GameState.getInstance().isLabCompleted(LabType.CHEMISTRY))
            {
                GameState.getInstance().completeLab(LabType.CHEMISTRY);
                DebugLog.log("Chemistry Lab completed!");
                
                // Add return arrow when lab is completed
                addObject(new DirectionArrow("up", "ÎNAPOI LA CLASĂ"), getWidth() / 2, 60);
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
        
        // Character's map position (adjusted by scroll)
        int mapX = character.getX() + scrollX;
        int mapY = character.getY() + scrollY;
        
        // Transition to MainMapWorld when inside the exit window (bottom-left area)
        if (mapX >= 0 && mapX <= 72 && mapY >= 551 && mapY <= 599)
        {
            DebugLog.log("TRANSITION TO MainMapWorld TRIGGERED!");
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
     * Refresh the Over-Player layer viewport so it scrolls with the camera.
     */
    private void updateOverlayImage()
    {
        if (overPlayerLayerImage == null || overlayActor == null || overPlayerViewport == null)
        {
            return;
        }

        overPlayerViewport.clear();
        overPlayerViewport.drawImage(overPlayerLayerImage, -scrollX, -scrollY);
        overlayActor.setImage(overPlayerViewport);
        overlayActor.setLocation(getWidth() / 2, getHeight() / 2);
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
    
    /**
     * Add a quest block at map coordinates that creates a collision until quest is completed
     */
    private void addQuestBlock(int mapX, int mapY)
    {
        QuestBlock block = new QuestBlock(mapX, mapY);
        questBlocks.add(block);
        
        // Add collision rectangle for this block
        if (tiledMap != null)
        {
            TiledMap.CollisionRect questCollision = new TiledMap.CollisionRect(
                mapX - 24,  // x - Center the collision (48px block)
                mapY - 24,  // y
                48,         // width
                48          // height
            );
            tiledMap.collisionRects.add(questCollision);
        }
        
        // Add the block to world at screen position
        // We'll update its position in act() based on scroll
        addObject(block, 0, 0);
        updateQuestBlockPositions();
    }
    
    /**
     * Remove the quest block collision when quest is completed
     */
    public void removeQuestBlockCollision(int mapX, int mapY)
    {
        if (tiledMap == null) return;
        
        // Find and remove the collision rectangle for this quest block
        Iterator<TiledMap.CollisionRect> iterator = tiledMap.collisionRects.iterator();
        while (iterator.hasNext())
        {
            TiledMap.CollisionRect rect = iterator.next();
            // Check if this collision rect matches the quest block position
            if (Math.abs(rect.x - (mapX - 24)) < 5 && 
                Math.abs(rect.y - (mapY - 24)) < 5 &&
                rect.w == 48 && rect.h == 48)
            {
                iterator.remove();
                DebugLog.log("Removed quest block collision at map: (" + mapX + ", " + mapY + ")");
                break;
            }
        }
    }
    
    /**
     * Update quest block positions based on current scroll
     */
    private void updateQuestBlockPositions()
    {
        for (QuestBlock block : questBlocks)
        {
            if (block.getWorld() != null && block.isActive())
            {
                // Convert map coordinates to screen coordinates
                int screenX = block.getMapX() - scrollX;
                int screenY = block.getMapY() - scrollY;
                block.setLocation(screenX, screenY);
            }
        }
    }
}
