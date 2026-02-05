import greenfoot.*;

/**
 * PrecisionHoldQuest - Hold LEFT for exact duration with increasing difficulty
 * Features: Purple glowing aura, tolerance zone visualization, progress bars, difficulty scaling
 */
public class PrecisionHoldQuest extends Actor
{
    private int mapX, mapY;
    private int holdTime = 0;
    private int targetTime = 120; // 2 seconds at 60 FPS
    private int tolerance = 12; // ±0.2 seconds
    private int baseTargetTime = 120;
    private int targetRounds = 3;
    private int levelCount = 0;
    private int totalScore = 0;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int successFlash = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean holdingStarted = false;
    private boolean tutorialActive = false;
    
    public PrecisionHoldQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        createImage();
    }
    
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
        
        // Check for player proximity and SPACE key to start
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
                        holdTime = 0;
                        baseTargetTime = 120;
                        targetTime = baseTargetTime;
                        tolerance = 12;
                        levelCount = 0;
                        totalScore = 0;
                        animTick = 0;
                        successFlash = 0;
                        holdingStarted = false;
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
        if (successFlash > 0) successFlash--;
        
        if (questActive)
        {
            animTick++;
            if (Greenfoot.isKeyDown("left"))
            {
                holdingStarted = true;
                holdTime++;
                
                // Perfect zone pulse feedback
                if (holdTime >= (targetTime - tolerance) && holdTime <= (targetTime + tolerance))
                {
                    successFlash = 5;
                }
            }
            else if (holdingStarted)
            {
                // Released, check result
                finishHold();
            }
            
            // Max out at 5 seconds to prevent overshoot
            if (holdTime > 300)
            {
                finishHold();
            }
            
            updateDisplay();
        }
    }

    public boolean isCompleted()
    {
        return completed;
    }
    
    private void finishHold()
    {
        boolean success = holdTime >= (targetTime - tolerance) && holdTime <= (targetTime + tolerance);
        
        if (success)
        {
            // Calculate precision score
            int distance = Math.abs(holdTime - targetTime);
            int points = 500 - (distance * 10);
            totalScore += Math.max(100, points);
            levelCount++;
            
            // Increase difficulty for next level
            baseTargetTime += 40; // 0.67 seconds harder each time
            targetTime = baseTargetTime;
            tolerance = Math.max(8, tolerance - 1); // Tolerance decreases
            
            if (levelCount >= targetRounds)
            {
                questActive = false;
                completed = true;
                GameState.getInstance().setMiniQuestActive(false);
                finishQuest(true);
            }
            else
            {
                holdTime = 0;
                holdingStarted = false;
                successFlash = 10;
            }
        }
        else
        {
            questActive = false;
            completed = true;
            GameState.getInstance().setMiniQuestActive(false);
            finishQuest(false);
        }
    }
    
    private void updateDisplay()
    {
        World world = getWorld();
        
        if (world != null)
        {
            if (myOverlay == null || myOverlay.getWorld() == null)
            {
                myOverlay = new OverlayLayer();
                world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
            }
        }

        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        int px = 0;
        int py = 0;

        // Animated pulsing background
        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        // Glowing aura effect (purple: 180, 100, 220)
        img.setColor(new Color(180, 100, 220, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel background
        img.setColor(new Color(15, 10, 25, 220));
        img.fillRect(px, py, panelW, panelH);

        // Double-line fancy borders with purple glow
        img.setColor(new Color(180, 100, 220, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(200, 150, 230, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("MENȚINERE PRECISĂ", px + 70, py + 50);

        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(200, 180, 220));
        img.drawString("INSTRUCȚIUNI: ține apăsată săgeata STÂNGA exact cât trebuie", px + 20, py + 70);

        // Difficulty level
        img.setColor(new Color(200, 150, 220));
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        img.drawString("NIVEL " + (levelCount + 1) + " | ȚINTĂ: " + (targetTime / 60.0f) + "s", px + 140, py + 90);

        // Tolerance zone visualization
        int progressBarWidth = panelW - 80;
        int progressBarHeight = 40;
        int barX = px + 40;
        int barY = py + 120;
        
        // Dark background
        img.setColor(new Color(40, 30, 50, 200));
        img.fillRect(barX, barY, progressBarWidth, progressBarHeight);

        // Red zone (too short)
        img.setColor(new Color(200, 50, 50, 120));
        img.fillRect(barX, barY, (targetTime - tolerance) * progressBarWidth / 300, progressBarHeight);

        // Green perfect zone
        img.setColor(new Color(100, 220, 100, 180));
        int perfectStart = (targetTime - tolerance) * progressBarWidth / 300;
        int perfectWidth = (tolerance * 2) * progressBarWidth / 300;
        img.fillRect(barX + perfectStart, barY, perfectWidth, progressBarHeight);

        // Red zone (too long)
        img.setColor(new Color(200, 50, 50, 120));
        int redStart = (targetTime + tolerance) * progressBarWidth / 300;
        img.fillRect(barX + redStart, barY, progressBarWidth - redStart, progressBarHeight);

        // Current progress indicator
        int currentPos = holdTime * progressBarWidth / 300;
        if (holdTime <= 300)
        {
            Color posColor;
            if (holdTime >= (targetTime - tolerance) && holdTime <= (targetTime + tolerance))
            {
                // In perfect zone - pulsing glow
                int glow = 150 + (int)(100 * Math.sin(animTick * 0.2));
                posColor = new Color(100, 255, 100);
                img.setColor(new Color(100, 255, 100, glow / 2));
                img.fillRect(barX + currentPos - 15, barY - 10, 30, progressBarHeight + 20);
            }
            else
            {
                posColor = new Color(255, 150, 100);
            }
            
            img.setColor(posColor);
            img.fillRect(barX + currentPos - 3, barY - 8, 6, progressBarHeight + 16);
        }

        // Zone labels
        img.setColor(new Color(180, 180, 180));
        img.setFont(new greenfoot.Font("Arial", true, false, 12));
        img.drawString("PREA SCURT", barX + 5, barY - 5);
        img.drawString("PERFECT", barX + progressBarWidth / 2 - 25, barY - 5);
        img.drawString("PREA LUNG", barX + progressBarWidth - 60, barY - 5);

        // Time display
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        float seconds = holdTime / 60.0f;
        float targetSeconds = targetTime / 60.0f;
        img.drawString(String.format("ȚINUT: %.2f / %.2f sec", seconds, targetSeconds), px + 110, py + 220);

        // Score display
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString("Scor: " + totalScore, px + 175, py + 250);

        if (myOverlay != null)
        {
            myOverlay.setImage(img);
        }
    }
    
    private void finishQuest(boolean success)
    {
        World world = getWorld();
        if (world == null || myOverlay == null) return;
        
        resultDisplayTicks = 120;
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        if (success)
        {
            img.setColor(new Color(180, 100, 220, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(200, 100, 255, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("PERFECT!", panelW / 2 - 100, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.setColor(new Color(100, 255, 200));
            img.drawString("Scor: " + totalScore, panelW / 2 - 70, panelH / 2 + 15);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Durată ținută: " + String.format("%.2f sec", holdTime / 60.0f), panelW / 2 - 140, panelH / 2 + 50);
        }
        else
        {
            img.setColor(new Color(180, 100, 220, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(200, 100, 255, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("EȘUAT!", panelW / 2 - 90, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.setColor(new Color(255, 150, 200));
            img.drawString("Ai ținut prea " + (holdTime < targetTime ? "PUȚIN" : "MULT"), panelW / 2 - 120, panelH / 2 + 10);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 14));
            img.setColor(new Color(200, 200, 200));
            float held = holdTime / 60.0f;
            float target = targetTime / 60.0f;
            img.drawString("Tu: " + String.format("%.2f sec", held), panelW / 2 - 60, panelH / 2 + 45);
            img.drawString("Țintă: " + String.format("%.2f sec", target), panelW / 2 - 80, panelH / 2 + 65);
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
        img.setColor(new Color(180, 120, 220, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: MENȚINERE", 130, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Ține apăsată săgeata STÂNGA exact cât trebuie.", 40, 70);
        img.drawString("Scop: intră în zona verde.", 130, 95);
        img.setColor(new Color(200, 255, 200));
        img.drawString("Apasă SPATIU pentru a începe", 140, 145);

        myOverlay.setImage(img);
    }
    
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }
    
    public java.util.List<TiledMap.CollisionRect> getCollisionRects()
    {
        return java.util.Collections.emptyList();
    }
}
