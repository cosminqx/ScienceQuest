import greenfoot.*;

/**
 * DirectionDodgeQuest - Press arrow keys as they appear with animated growth, catch zones, and difficulty progression
 */
public class DirectionDodgeQuest extends Actor
{
    private int mapX, mapY;
    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private int targetArrows = 12;
    private int arrowsCaught = 0;
    private int arrowsMissed = 0;
    private int difficulty = 1;
    private boolean questActive = false;
    private boolean completed = false;
    private boolean success = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int resultScreenTick = 0;
    private int spawnTimer = 0;
    private int spawnDelay = 80;
    private int feedbackTick = 0;
    private String currentArrow = null;
    private int arrowAppearTick = 0;
    private int arrowGrowth = 0;
    private int catchZoneStart = 25;
    private int catchZoneEnd = 40;
    private int catchZoneSize = catchZoneEnd - catchZoneStart;
    private boolean lastWasHit = false;
    
    public DirectionDodgeQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        createImage();
    }
    
    private void createImage()
    {
        GreenfootImage img = new GreenfootImage(48, 48);
        img.setColor(new Color(0, 0, 0, 0));
        img.fillRect(0, 0, 48, 48);
        setImage(img);
    }
    
    public void act()
    {
        if (completed) return;
        
        Actor player = getPlayer();
        if (player != null && !questActive)
        {
            int dx = Math.abs(player.getX() - getX());
            int dy = Math.abs(player.getY() - getY());
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < 100 && interactionCooldown == 0 && Greenfoot.isKeyDown("space"))
            {
                questActive = true;
                score = 0;
                combo = 0;
                maxCombo = 0;
                arrowsCaught = 0;
                arrowsMissed = 0;
                difficulty = 1;
                spawnTimer = 0;
                spawnDelay = 80;
                currentArrow = null;
                animTick = 0;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        if (feedbackTick > 0) feedbackTick--;
        
        if (questActive && !completed)
        {
            animTick++;
            spawnTimer++;
            if (currentArrow != null) arrowAppearTick++;
            
            // Adjust difficulty
            if (arrowsCaught > 0 && arrowsCaught % 4 == 0)
            {
                difficulty = 2 + (arrowsCaught / 4);
                spawnDelay = Math.max(40, 80 - (difficulty * 8));
            }
            
            if (spawnTimer >= spawnDelay && currentArrow == null)
            {
                spawnNewArrow();
                spawnTimer = 0;
            }
            
            // Check if arrow should be missed
            if (currentArrow != null && arrowAppearTick > 60)
            {
                arrowMissed();
            }
            
            checkInput();
            
            if (arrowsCaught >= targetArrows)
            {
                finishQuest(true);
            }
            
            updateDisplay();
        }
        else if (completed)
        {
            resultScreenTick++;
            updateDisplay();
            if (resultScreenTick > 120)
            {
                GameState.getInstance().setMiniQuestActive(false);
            }
        }
    }
    
    private void spawnNewArrow()
    {
        String[] directions = {"up", "down", "left", "right"};
        currentArrow = directions[Greenfoot.getRandomNumber(4)];
        arrowAppearTick = 0;
        arrowGrowth = 0;
    }
    
    private void checkInput()
    {
        if (currentArrow == null) return;
        
        boolean keyPressed = false;
        if (currentArrow.equals("up") && Greenfoot.isKeyDown("up")) keyPressed = true;
        else if (currentArrow.equals("down") && Greenfoot.isKeyDown("down")) keyPressed = true;
        else if (currentArrow.equals("left") && Greenfoot.isKeyDown("left")) keyPressed = true;
        else if (currentArrow.equals("right") && Greenfoot.isKeyDown("right")) keyPressed = true;
        
        if (keyPressed && arrowAppearTick >= catchZoneStart && arrowAppearTick <= catchZoneEnd)
        {
            int timeInZone = arrowAppearTick - catchZoneStart;
            int centerOffset = catchZoneSize / 2;
            int distanceFromCenter = Math.abs(timeInZone - centerOffset);
            int earnedPoints = Math.max(1, 3 - (distanceFromCenter / 5));
            
            score += earnedPoints;
            combo++;
            if (combo > maxCombo) maxCombo = combo;
            arrowsCaught++;
            feedbackTick = 12;
            lastWasHit = true;
            currentArrow = null;
        }
    }
    
    private void arrowMissed()
    {
        if (currentArrow != null)
        {
            combo = 0;
            arrowsMissed++;
            lastWasHit = false;
            currentArrow = null;
        }
    }
    
    private void updateDisplay()
    {
        World world = getWorld();
        if (world == null) return;
        
        // Get or create overlay layer
        java.util.List<OverlayLayer> overlays = world.getObjects(OverlayLayer.class);
        OverlayLayer overlay;
        if (overlays.isEmpty()) {
            overlay = new OverlayLayer();
            world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        } else {
            overlay = overlays.get(0);
        }
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        int pulse = 75 + (int)(45 * Math.sin(animTick * 0.09));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);
        
        if (completed)
        {
            drawResultScreen(img, panelW, panelH);
        }
        else
        {
            drawQuestScreen(img, panelW, panelH);
        }
        
        setImage(img);
        overlay.setImage(img);
    }
    
    private void drawQuestScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowAlpha = 65 + (int)(35 * Math.sin(animTick * 0.07));
        img.setColor(new Color(100, 150, 255, glowAlpha));
        img.fillRect(px - 12, py - 12, panelW + 24, panelH + 24);
        
        // Panel background
        img.setColor(new Color(10, 15, 35, 250));
        img.fillRect(px, py, panelW, panelH);
        
        // Border with glow layers
        img.setColor(new Color(100, 150, 255, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(120, 160, 255, 140));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);
        img.setColor(new Color(140, 170, 255, 70));
        img.drawRect(px + 2, py + 2, panelW - 4, panelH - 4);
        
        // Title
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 34));
        img.drawString("DIRECTION DODGE", px + 70, py + 50);
        
        // Stats
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(new Color(120, 255, 150));
        img.drawString("Caught: " + arrowsCaught + "/" + targetArrows, px + 40, py + 105);
        
        img.setColor(new Color(255, 200, 100));
        img.drawString("Score: " + score, px + 40, py + 135);
        
        img.setColor(new Color(150, 200, 255));
        img.drawString("Combo: " + combo + " | Best: " + maxCombo, px + 40, py + 165);
        
        img.setColor(new Color(255, 150, 150));
        img.drawString("Difficulty: " + difficulty, px + 340, py + 105);
        
        // Progress bar
        int barW = 450;
        int barH = 32;
        int barX = px + 45;
        int barY = py + 210;
        
        img.setColor(new Color(60, 60, 80, 200));
        img.fillRect(barX - 4, barY - 4, barW + 8, barH + 8);
        img.setColor(new Color(100, 150, 255, 180));
        img.drawRect(barX - 4, barY - 4, barW + 8, barH + 8);
        img.setColor(new Color(30, 40, 70));
        img.fillRect(barX, barY, barW, barH);
        
        int progress = (int)((arrowsCaught * 1.0 / targetArrows) * barW);
        for (int i = 0; i < progress; i++)
        {
            int shade = 150 + (i * 105) / barW;
            img.setColor(new Color(100, shade, 255, 230));
            img.drawLine(barX + i, barY, barX + i, barY + barH);
        }
        
        // Arrow display area
        int arrowAreaX = px + 45;
        int arrowAreaY = py + 280;
        int arrowAreaW = 450;
        int arrowAreaH = 70;
        
        img.setColor(new Color(40, 50, 100, 100));
        img.fillRect(arrowAreaX, arrowAreaY, arrowAreaW, arrowAreaH);
        img.setColor(new Color(100, 150, 255, 150));
        img.drawRect(arrowAreaX, arrowAreaY, arrowAreaW, arrowAreaH);
        
        // Draw current arrow if active
        if (currentArrow != null)
        {
            int arrowX = arrowAreaX + arrowAreaW / 2;
            int arrowY = arrowAreaY + arrowAreaH / 2;
            
            // Arrow growth/size based on time
            int arrowSize = 20 + (arrowAppearTick * 2);
            arrowSize = Math.min(arrowSize, 70);
            
            // In catch zone?
            boolean inCatchZone = arrowAppearTick >= catchZoneStart && arrowAppearTick <= catchZoneEnd;
            
            // Color based on zone
            if (arrowAppearTick < catchZoneStart)
            {
                img.setColor(new Color(255, 255, 100, 200));
            }
            else if (inCatchZone)
            {
                int zoneFlash = 50 + (int)(50 * Math.sin(animTick * 0.2));
                img.setColor(new Color(100, 255, 150, 200 + zoneFlash));
            }
            else
            {
                img.setColor(new Color(255, 150, 100, 200));
            }
            
            img.setFont(new greenfoot.Font("Arial", true, false, arrowSize));
            String arrowChar = getArrowChar(currentArrow);
            drawCenteredString(img, arrowChar, arrowX, arrowY, arrowSize);
            
            // Draw catch zone indicator
            if (arrowAppearTick < catchZoneStart)
            {
                img.setColor(new Color(100, 255, 150, 100));
                img.drawString("Get Ready...", arrowAreaX + 150, arrowAreaY + 55);
            }
            else if (inCatchZone)
            {
                img.setColor(new Color(100, 255, 150, 255));
                img.setFont(new greenfoot.Font("Arial", true, false, 16));
                img.drawString("NOW!", arrowAreaX + 190, arrowAreaY + 55);
            }
            else
            {
                img.setColor(new Color(255, 100, 100, 200));
                img.drawString("Missed!", arrowAreaX + 170, arrowAreaY + 55);
            }
        }
        else
        {
            img.setColor(new Color(150, 150, 200, 150));
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("Waiting for arrow...", arrowAreaX + 140, arrowAreaY + 40);
        }
        
        // Flash feedback on hit
        if (feedbackTick > 0 && lastWasHit)
        {
            int flashAlpha = (feedbackTick * 200) / 12;
            img.setColor(new Color(100, 255, 150, flashAlpha / 2));
            img.fillRect(px + 10, py + 10, panelW - 20, panelH - 20);
        }
    }
    
    private void drawResultScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowColor = success ? 100 : 200;
        int glowColorG = success ? 255 : 120;
        int glowColorB = success ? 100 : 100;
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 75));
        img.fillRect(px - 10, py - 10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(15, 20, 40, 250));
        img.fillRect(px, py, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 210));
        img.drawRect(px, py, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 36));
        String resultText = success ? "SUCCESS!" : "COMPLETE!";
        img.drawString(resultText, px + 90, py + 60);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("Final Score: " + score, px + 70, py + 120);
        img.drawString("Arrows Caught: " + arrowsCaught, px + 70, py + 155);
        img.drawString("Max Combo: " + maxCombo, px + 70, py + 190);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.drawString("Reached Difficulty: " + difficulty, px + 110, py + 255);
    }
    
    private String getArrowChar(String direction)
    {
        switch (direction)
        {
            case "up": return "▲";
            case "down": return "▼";
            case "left": return "◀";
            case "right": return "▶";
            default: return "?";
        }
    }
    
    private void drawCenteredString(GreenfootImage img, String str, int x, int y, int size)
    {
        int strWidth = size * str.length() / 2;
        img.drawString(str, x - strWidth / 2, y + size / 4);
    }
    
    private void finishQuest(boolean success)
    {
        this.success = success;
        completed = true;
        questActive = false;
        resultScreenTick = 0;
        
        World world = getWorld();
        if (world == null) return;
        
        // Get overlay layer
        java.util.List<OverlayLayer> overlays = world.getObjects(OverlayLayer.class);
        if (overlays.isEmpty()) return;
        OverlayLayer overlay = overlays.get(0);
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        // Glow effect
        int glowColor = success ? 100 : 200;
        int glowColorG = success ? 255 : 120;
        int glowColorB = success ? 100 : 100;
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 75));
        img.fillRect(-10, -10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(15, 20, 40, 250));
        img.fillRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 210));
        img.drawRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 32));
        String resultText = success ? "SUCCESS!" : "COMPLETE!";
        img.drawString(resultText, panelW / 2 - 70, panelH / 2 - 30);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Final Score: " + score, panelW / 2 - 90, panelH / 2 + 20);
        img.drawString("Arrows Caught: " + arrowsCaught, panelW / 2 - 110, panelH / 2 + 50);
        img.drawString("Max Combo: " + maxCombo, panelW / 2 - 85, panelH / 2 + 80);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", false, false, 13));
        img.drawString("Reached Difficulty: " + difficulty, panelW / 2 - 95, panelH / 2 + 115);
        
        // Set transparent actor image
        GreenfootImage transparent = new GreenfootImage(48, 48);
        transparent.setColor(new Color(0, 0, 0, 0));
        transparent.fillRect(0, 0, 48, 48);
        setImage(transparent);
        
        overlay.setImage(img);
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
    
    public java.awt.Rectangle[] getCollisionRects()
    {
        return new java.awt.Rectangle[] { new java.awt.Rectangle(getX() - 24, getY() - 24, 48, 48) };
    }
}
