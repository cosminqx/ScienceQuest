import greenfoot.*;

/**
 * KeySequenceQuest - Complete key sequences with visual feedback
 */
public class KeySequenceQuest extends Actor
{
    private int mapX, mapY;
    private int sequenceIndex = 0;
    private int targetSequences = 3;
    private int completedSequences = 0;
    private String[] sequence = new String[3];
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int resetCooldown = 0;
    private int animTick = 0;
    private int correctFeedbackTick = 0;
    private int wrongFeedbackTick = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    
    public KeySequenceQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        generateSequence();
        createImage();
    }
    
    private void generateSequence()
    {
        String[] keys = {"up", "down", "left", "right"};
        for (int i = 0; i < 3; i++)
        {
            sequence[i] = keys[Greenfoot.getRandomNumber(4)];
        }
    }
    
    private void createImage()
    {
        GreenfootImage img = new GreenfootImage("exclamation-mark.png");
        img.scale(32, 32);
        GreenfootImage marker = new GreenfootImage(48, 48);
        marker.setColor(new Color(0, 0, 0, 0));
        marker.fillRect(0, 0, 48, 48);
        marker.drawImage(img, 8, 0);
        marker.setColor(new Color(255, 255, 255));
        marker.setFont(new greenfoot.Font("Arial", true, false, 10));
        marker.drawString("SPACE", 6, 46);
        setImage(marker);
    }
    
    public void act()
    {
        if (completed)
        {
            if (resultDisplayTicks > 0)
            {
                resultDisplayTicks--;
                if (resultDisplayTicks == 0)
                {
                    clearOverlay();
                }
            }
            return;
        }

        initBasePosition();
        if (!questActive)
        {
            updateFloating();
        }
        
        Actor player = getPlayer();
        if (player != null && !questActive)
        {
            int dx = Math.abs(player.getX() - getX());
            int dy = Math.abs(player.getY() - getY());
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            boolean startPressed = Greenfoot.isKeyDown("space");
            if (distance < 100 && interactionCooldown == 0 && startPressed && !startKeyDown)
            {
                questActive = true;
                sequenceIndex = 0;
                completedSequences = 0;
                animTick = 0;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
            startKeyDown = startPressed;
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        if (resetCooldown > 0) resetCooldown--;
        if (correctFeedbackTick > 0) correctFeedbackTick--;
        if (wrongFeedbackTick > 0) wrongFeedbackTick--;
        
        if (questActive)
        {
            animTick++;
            checkInput();
            updateDisplay();
        }
    }
    
    private void checkInput()
    {
        String currentKey = sequence[sequenceIndex];
        boolean keyPressed = false;
        
        if (Greenfoot.isKeyDown(currentKey))
        {
            keyPressed = true;
        }
        
        if (keyPressed && resetCooldown == 0)
        {
            sequenceIndex++;
            correctFeedbackTick = 8;
            resetCooldown = 10;
            
            if (sequenceIndex >= sequence.length)
            {
                completedSequences++;
                if (completedSequences >= targetSequences)
                {
                    finishQuest(true);
                }
                else
                {
                    generateSequence();
                    sequenceIndex = 0;
                }
            }
        }
    }
    
    private void updateDisplay()
    {
        World world = getWorld();
        if (world == null) return;
        
        // Get or create overlay layer
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);

        int pulse = 100 + (int)(60 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        int px = 0;
        int py = 0;

        // Glow effect
        img.setColor(new Color(100, 150, 255, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel
        img.setColor(new Color(10, 10, 20, 245));
        img.fillRect(px, py, panelW, panelH);

        img.setColor(new Color(100, 150, 255, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(120, 170, 255, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("SEQUENCE", px + 130, py + 50);

        // Current key to press
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        String keyStr = sequence[sequenceIndex].toUpperCase();
        String arrow = getArrowForKey(sequence[sequenceIndex]);
        
        img.setColor(correctFeedbackTick > 0 ? new Color(100, 255, 100) : new Color(200, 200, 200));
        img.drawString("Press: " + keyStr + " " + arrow, px + 95, py + 110);

        // Sequence counter
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 200, 100));
        img.drawString("Sequences: " + completedSequences + " / " + targetSequences, px + 90, py + 155);
        img.drawString("Step: " + (sequenceIndex + 1) + " / 3", px + 135, py + 185);

        // Progress bar for current sequence
        float progress = (sequenceIndex + 1) / 3.0f;
        int barW = (int)((panelW - 40) * progress);
        img.setColor(new Color(100, 200, 255, 150));
        img.fillRect(px + 20, py + 220, barW, 15);
        img.setColor(new Color(120, 220, 255, 200));
        img.drawRect(px + 20, py + 220, panelW - 40, 15);

        setImage(img);
        myOverlay.setImage(img);
    }
    
    private String getArrowForKey(String key)
    {
        if (key.equals("up")) return "▲";
        if (key.equals("down")) return "▼";
        if (key.equals("left")) return "◀";
        if (key.equals("right")) return "▶";
        return "";
    }
    
    private void finishQuest(boolean success)
    {
        questActive = false;
        completed = true;
        resultDisplayTicks = 120;
        GameState.getInstance().setMiniQuestActive(false);

        World world = getWorld();
        if (world == null || myOverlay == null) return;
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        int score = completedSequences * 150;

        if (success)
        {
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("SUCCESS!", panelW / 2 - 120, panelH / 2 - 30);
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.drawString("Score: " + score, panelW / 2 - 80, panelH / 2 + 50);
        }
        
        // Set transparent actor image
        GreenfootImage transparent = new GreenfootImage(48, 48);
        transparent.setColor(new Color(0, 0, 0, 0));
        transparent.fillRect(0, 0, 48, 48);
        setImage(transparent);
        
        myOverlay.setImage(img);
    }
    
    private void clearOverlay()
    {
        if (myOverlay != null && myOverlay.getWorld() != null)
        {
            getWorld().removeObject(myOverlay);
            myOverlay = null;
        }
    }

    private void initBasePosition()
    {
        if (!baseYSet && getWorld() != null)
        {
            baseY = getY();
            baseYSet = true;
        }
    }

    private void updateFloating()
    {
        if (!baseYSet) return;
        floatTick++;
        int offset = (int)(Math.sin(floatTick * 0.12) * 4);
        setLocation(getX(), baseY + offset);
    }
    
    private Actor getPlayer()
    {
        World world = getWorld();
        if (world == null) return null;
        
        java.util.List<Boy> boys = world.getObjects(Boy.class);
        if (!boys.isEmpty()) return boys.get(0);
        
        java.util.List<Girl> girls = world.getObjects(Girl.class);
        if (!girls.isEmpty()) return girls.get(0);
        
        return null;
    }
    
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }
    
    public java.util.List<TiledMap.CollisionRect> getCollisionRects()
    {
        return java.util.Collections.emptyList();
    }
}
