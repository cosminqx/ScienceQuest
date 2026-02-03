import greenfoot.*;

/**
 * RapidFireQuest - Mash SPACE with combo tracking and stylish UI
 */
public class RapidFireQuest extends Actor
{
    private int mapX, mapY;
    private int spaceCount = 0;
    private int targetCount = 40;
    private int timeRemaining = 300;
    private int timeMax = 300;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int pressFeedbackTick = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean promptActive = false;
    private boolean startKeyDown = false;
    
    public RapidFireQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        createImage();
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
            if (distance < 100)
            {
                showStartPrompt("RAPID FIRE");
                if (interactionCooldown == 0 && startPressed && !startKeyDown)
                {
                    promptActive = false;
                    questActive = true;
                    animTick = 0;
                    spaceCount = 0;
                    combo = 0;
                    maxCombo = 0;
                    timeRemaining = timeMax;
                    GameState.getInstance().setMiniQuestActive(true);
                    interactionCooldown = 10;
                }
            }
            else if (promptActive)
            {
                promptActive = false;
                clearOverlay();
            }
            startKeyDown = startPressed;
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            if (pressFeedbackTick > 0) pressFeedbackTick--;
            
            if (Greenfoot.isKeyDown("space"))
            {
                spaceCount++;
                combo++;
                pressFeedbackTick = 6;
                if (combo > maxCombo) maxCombo = combo;
            }
            else
            {
                combo = 0;
            }
            
            timeRemaining--;
            updateDisplay();
            
            if (timeRemaining <= 0)
            {
                finishQuest();
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

        // Dynamic background pulse
        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        int px = 0;
        int py = 0;

        // Glowing outer aura
        img.setColor(new Color(255, 100, 80, 50));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);
        img.setColor(new Color(255, 150, 100, 30));
        img.fillRect(px - 12, py - 12, panelW + 24, panelH + 24);

        // Panel background
        img.setColor(new Color(10, 10, 20, 245));
        img.fillRect(px, py, panelW, panelH);

        // Gradient border effect
        img.setColor(new Color(255, 120, 80, 240));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(255, 150, 100, 140));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Progress bar section
        int barY = py + 140;
        img.setColor(new Color(35, 35, 45, 200));
        img.fillRect(px + 20, barY, panelW - 40, 28);

        // Color based on progress
        float progress = spaceCount / (float)targetCount;
        Color barColor = getProgressColor(progress);
        
        // Bar glow effect
        int barW = (int)((panelW - 40) * progress);
        img.setColor(new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 70));
        img.fillRect(px + 18, barY - 3, barW + 4, 34);

        // Main bar
        img.setColor(barColor);
        img.fillRect(px + 20, barY, barW, 28);

        // Feedback flash on press
        if (pressFeedbackTick > 0)
        {
            img.setColor(new Color(255, 255, 200, pressFeedbackTick * 40));
            img.fillRect(px + 20, barY, barW + 2, 28);
        }

        // Border
        img.setColor(new Color(100, 120, 150, 180));
        img.drawRect(px + 20, barY, panelW - 40, 28);

        // Title and stats
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("RAPID FIRE", px + 115, py + 45);
        
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.drawString("Presses: " + spaceCount + " / " + targetCount, px + 100, py + 85);
        img.drawString("Combo: " + combo + " (Max: " + maxCombo + ")", px + 105, py + 110);
        
        // Time bar
        float timePct = timeRemaining / (float)timeMax;
        int timeBarW = (int)((panelW - 40) * timePct);
        img.setColor(new Color(80, 150, 255, 80));
        img.fillRect(px + 20, py + 180, timeBarW, 10);
        img.setColor(new Color(100, 170, 255, 180));
        img.drawRect(px + 20, py + 180, panelW - 40, 10);
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        img.drawString(Math.round(timeRemaining / 60.0f) + "s", px + panelW - 50, py + 205);

        myOverlay.setImage(img);
    }
    
    private Color getProgressColor(float progress)
    {
        if (progress < 0.25f) return new Color(255, 80, 80);
        if (progress < 0.5f) return new Color(255, 180, 80);
        if (progress < 0.75f) return new Color(255, 240, 80);
        return new Color(80, 255, 100);
    }
    
    private void finishQuest()
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
        int score = spaceCount * 25 + maxCombo * 15;
        
        if (spaceCount >= targetCount)
        {
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);

            // Success glow aura
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("SUCCESS!", panelW / 2 - 120, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.drawString("Score: " + score + " | Combo: " + maxCombo, panelW / 2 - 140, panelH / 2 + 50);
        }
        else
        {
            img.setColor(new Color(200, 0, 50, 190));
            img.fillRect(0, 0, panelW, panelH);

            // Failure glow
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("FAILED!", panelW / 2 - 110, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
            img.drawString("Need " + (targetCount - spaceCount) + " more!", panelW / 2 - 100, panelH / 2 + 50);
            img.drawString("Score: " + score, panelW / 2 - 60, panelH / 2 + 75);
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

    private void showStartPrompt(String title)
    {
        World world = getWorld();
        if (world == null) return;
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }

        int w = 320;
        int h = 90;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 170));
        img.fillRect(0, 0, w, h);
        img.setColor(new Color(255, 255, 255, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(Color.WHITE);
        img.drawString(title, 20, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(200, 200, 200));
        img.drawString("Press SPACE to start", 70, 60);

        myOverlay.setImage(img);
        promptActive = true;
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
