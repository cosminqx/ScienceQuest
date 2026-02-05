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
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    
    public DirectionDodgeQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        createImage();
    }
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean anyArrowDown = false;
    private boolean tutorialActive = false;
    
    private void createImage()
    {
        GreenfootImage img = new GreenfootImage("exclamation-mark.png");
        int maxSize = 32;
        int imgW = img.getWidth();
        int imgH = img.getHeight();
        if (imgW >= imgH)
        {
            int scaledH = (int)Math.round(imgH * (maxSize / (double)imgW));
            img.scale(maxSize, Math.max(1, scaledH));
        }
        else
        {
            int scaledW = (int)Math.round(imgW * (maxSize / (double)imgH));
            img.scale(Math.max(1, scaledW), maxSize);
        }
        GreenfootImage marker = new GreenfootImage(48, 48);
        marker.setColor(new Color(0, 0, 0, 0));
        marker.fillRect(0, 0, 48, 48);
        int drawX = (48 - img.getWidth()) / 2;
        int drawY = Math.max(0, (32 - img.getHeight()) / 2);
        marker.drawImage(img, drawX, drawY);
        marker.setColor(new Color(255, 255, 255));
        marker.setFont(new greenfoot.Font("Arial", true, false, 10));
        marker.drawString("SPATIU", 4, 46);
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
                if (!tutorialActive)
                {
                    if (interactionCooldown == 0 && startPressed && !startKeyDown)
                    {
                        tutorialActive = true;
                        showTutorial();
                        interactionCooldown = 10;
                    }
                }
                else
                {
                    showTutorial();
                    if (interactionCooldown == 0 && startPressed && !startKeyDown)
                    {
                        tutorialActive = false;
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
                        arrowAppearTick = 0;
                        arrowGrowth = 0;
                        animTick = 0;
                        anyArrowDown = false;
                        GameState.getInstance().setMiniQuestActive(true);
                        interactionCooldown = 10;
                    }
                }
            }
            else if (tutorialActive)
            {
                tutorialActive = false;
                clearOverlay();
            }
            startKeyDown = startPressed;
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
            if (!questActive) return;
            
            if (arrowsCaught >= targetArrows)
            {
                finishQuest(true);
            }
            
            updateDisplay();
        }
    }

    public boolean isCompleted()
    {
        return completed;
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
        
        boolean up = Greenfoot.isKeyDown("up");
        boolean down = Greenfoot.isKeyDown("down");
        boolean left = Greenfoot.isKeyDown("left");
        boolean right = Greenfoot.isKeyDown("right");
        boolean anyDownNow = up || down || left || right;
        boolean keyPressed = false;
        if (currentArrow.equals("up") && up) keyPressed = true;
        else if (currentArrow.equals("down") && down) keyPressed = true;
        else if (currentArrow.equals("left") && left) keyPressed = true;
        else if (currentArrow.equals("right") && right) keyPressed = true;
        
        if (keyPressed && !anyArrowDown)
        {
            boolean inCatch = arrowAppearTick >= catchZoneStart && arrowAppearTick <= catchZoneEnd;
            int earnedPoints;
            if (inCatch)
            {
                int timeInZone = arrowAppearTick - catchZoneStart;
                int centerOffset = catchZoneSize / 2;
                int distanceFromCenter = Math.abs(timeInZone - centerOffset);
                earnedPoints = Math.max(2, 4 - (distanceFromCenter / 5));
            }
            else
            {
                earnedPoints = 1;
                combo = 0;
            }
            
            score += earnedPoints;
            if (inCatch) combo++;
            if (combo > maxCombo) maxCombo = combo;
            arrowsCaught++;
            feedbackTick = 12;
            lastWasHit = inCatch;
            currentArrow = null;
        }
        
        anyArrowDown = anyDownNow;
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
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
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
        myOverlay.setImage(img);
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
        img.setFont(new greenfoot.Font("Arial", true, false, 30));
        img.drawString("Evită DIRECȚIA", px + 80, py + 50);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(180, 220, 255));
        img.drawString("INSTRUCȚIUNI: apasă săgeata corectă când apare", px + 60, py + 75);
        
        // Stats
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(new Color(120, 255, 150));
        img.drawString("Prinse: " + arrowsCaught + "/" + targetArrows, px + 40, py + 105);
        
        img.setColor(new Color(255, 200, 100));
        img.drawString("Scor: " + score, px + 40, py + 135);
        
        img.setColor(new Color(150, 200, 255));
        img.drawString("Combo: " + combo + " | Max: " + maxCombo, px + 40, py + 165);
        
        img.setColor(new Color(255, 150, 150));
        img.drawString("Dificultate: " + difficulty, px + 310, py + 105);
        
        // Progress bar
        int barW = 450;
        int barH = 32;
        int barX = px + 45;
        int barY = py + 170;
        
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
        int arrowAreaY = py + 210;
        int arrowAreaW = 450;
        int arrowAreaH = 60;
        
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
                int zoneAlpha = Math.min(255, 200 + zoneFlash);
                img.setColor(new Color(100, 255, 150, zoneAlpha));
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
                img.drawString("Pregătește-te...", arrowAreaX + 130, arrowAreaY + 55);
            }
            else if (inCatchZone)
            {
                img.setColor(new Color(100, 255, 150, 255));
                img.setFont(new greenfoot.Font("Arial", true, false, 16));
                img.drawString("ACUM!", arrowAreaX + 185, arrowAreaY + 55);
            }
            else
            {
                img.setColor(new Color(255, 100, 100, 200));
                img.drawString("Ratat!", arrowAreaX + 175, arrowAreaY + 55);
            }
        }
        else
        {
            img.setColor(new Color(150, 150, 200, 150));
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("Aștept săgeata...", arrowAreaX + 140, arrowAreaY + 40);
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
        img.drawString("Scor final: " + score, px + 70, py + 120);
        img.drawString("Săgeți prinse: " + arrowsCaught, px + 70, py + 155);
        img.drawString("Combo maxim: " + maxCombo, px + 70, py + 190);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.drawString("Dificultate atinsă: " + difficulty, px + 95, py + 255);
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
        resultDisplayTicks = 120;
        GameState.getInstance().setMiniQuestActive(false);
        
        World world = getWorld();
        if (world == null || myOverlay == null) return;
        
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
        img.drawString("Scor final: " + score, panelW / 2 - 90, panelH / 2 + 20);
        img.drawString("Săgeți prinse: " + arrowsCaught, panelW / 2 - 120, panelH / 2 + 50);
        img.drawString("Combo maxim: " + maxCombo, panelW / 2 - 95, panelH / 2 + 80);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", false, false, 13));
        img.drawString("Dificultate atinsă: " + difficulty, panelW / 2 - 105, panelH / 2 + 115);
        
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

    private void showTutorial()
    {
        World world = getWorld();
        if (world == null) return;
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }

        int w = 460;
        int h = 190;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 200));
        img.fillRect(0, 0, w, h);
        img.setColor(new Color(120, 170, 255, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: DIRECȚII", 130, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Apasă săgeata afișată când intră în zona verde.", 35, 70);
        img.drawString("Scop: prinde " + targetArrows + " săgeți.", 140, 95);
        img.setColor(new Color(200, 255, 200));
        img.drawString("Apasă SPATIU pentru a începe", 140, 145);

        myOverlay.setImage(img);
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
    
    public java.awt.Rectangle[] getCollisionRects()
    {
        return new java.awt.Rectangle[] { new java.awt.Rectangle(getX() - 24, getY() - 24, 48, 48) };
    }
}
