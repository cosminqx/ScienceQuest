import greenfoot.*;

/**
 * RhythmReleaseQuest - Hold key at the right rhythm (3 levels with increasing difficulty)
 * Features: Red glowing aura, pulsing animations, target zone visualization, speed progression
 */
public class RhythmReleaseQuest extends Actor
{
    private int mapX, mapY;
    private int hitCount = 0;
    private int targetHits = 3;
    private int barWidth = 360;
    private int indicatorPos = 0;
    private int indicatorSpeed = 2;
    private boolean movingRight = true;
    private int successZoneStart = 150;
    private int successZoneWidth = 50;
    private int totalScore = 0;
    private int combo = 0;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int failCooldown = 0;
    private int animTick = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean spaceDown = false;
    private boolean tutorialActive = false;
    
    public RhythmReleaseQuest(int mapX, int mapY)
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
                        hitCount = 0;
                        totalScore = 0;
                        combo = 0;
                        indicatorSpeed = 2;
                        successZoneWidth = 50;
                        animTick = 0;
                        spaceDown = false;
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
        if (failCooldown > 0) failCooldown--;
        
        if (questActive)
        {
            animTick++;
            if (failCooldown == 0)
            {
                // Move indicator
                if (movingRight)
                {
                    indicatorPos += indicatorSpeed;
                    if (indicatorPos >= barWidth - 10)
                    {
                        indicatorPos = barWidth - 10;
                        movingRight = false;
                    }
                }
                else
                {
                    indicatorPos -= indicatorSpeed;
                    if (indicatorPos <= 0)
                    {
                        indicatorPos = 0;
                        movingRight = true;
                    }
                }
                
                boolean spacePressed = Greenfoot.isKeyDown("space");
                if (spacePressed && !spaceDown)
                {
                    int distance = Math.abs(indicatorPos - (successZoneStart + successZoneWidth / 2));
                    
                    if (indicatorPos >= successZoneStart && indicatorPos <= successZoneStart + successZoneWidth)
                    {
                        // Perfect hit
                        int points = 300 - (distance * 6);
                        totalScore += points;
                        hitCount++;
                        combo++;
                        failCooldown = 15;
                        
                        if (hitCount >= targetHits)
                        {
                            finishQuest(true);
                        }
                        else
                        {
                            // Increase difficulty
                            indicatorSpeed += 1;
                            successZoneWidth = Math.max(30, successZoneWidth - 3);
                        }
                    }
                    else if (Math.abs(indicatorPos - (successZoneStart + successZoneWidth / 2)) < 100)
                    {
                        // Good hit (just outside zone)
                        int points = 150 - (distance * 2);
                        totalScore += Math.max(0, points);
                        hitCount++;
                        combo++;
                        failCooldown = 20;
                        
                        if (hitCount >= targetHits)
                        {
                            finishQuest(true);
                        }
                        else
                        {
                            indicatorSpeed += 1;
                            successZoneWidth = Math.max(30, successZoneWidth - 3);
                        }
                    }
                    else
                    {
                        // Missed
                        combo = 0;
                        failCooldown = 40;
                    }
                }
                spaceDown = spacePressed;
            }
            
            updateDisplay();
        }
    }

    public boolean isCompleted()
    {
        return completed;
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

        // Animated pulsing background
        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        int px = 0;
        int py = 0;

        // Glowing aura effect (red: 255, 100, 100)
        img.setColor(new Color(255, 100, 100, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel background
        img.setColor(new Color(20, 15, 15, 220));
        img.fillRect(px, py, panelW, panelH);

        // Double-line fancy borders with red glow
        img.setColor(new Color(255, 100, 100, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(255, 150, 150, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("ELIBERARE PE RITM", px + 50, py + 50);

        // Speed indicator - difficulty progression
        img.setColor(new Color(255, 150, 150));
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        img.drawString("INSTRUCȚIUNI: apasă SPATIU când indicatorul e în zona verde", px + 20, py + 75);
        img.drawString("NIVEL: " + (hitCount + 1) + " | VITEZĂ: " + indicatorSpeed, px + 140, py + 95);

        // Bar background
        img.setColor(new Color(40, 40, 40, 200));
        img.fillRect(px + 50, py + 120, barWidth, 50);

        // Zone labels
        img.setColor(new Color(200, 200, 200));
        img.setFont(new greenfoot.Font("Arial", true, false, 12));
        img.drawString("RATAT", px + 45, py + 115);
        img.drawString("PERFECT", px + 160, py + 115);
        img.drawString("RATAT", px + 400, py + 115);

        // Red miss zone
        img.setColor(new Color(200, 50, 50, 100));
        img.fillRect(px + 50, py + 120, successZoneStart - 10, 50);
        img.fillRect(px + 50 + successZoneStart + successZoneWidth + 10, py + 120, 
                     barWidth - (successZoneStart + successZoneWidth + 10), 50);

        // Green success zone (perfect area)
        img.setColor(new Color(100, 200, 100, 180));
        img.fillRect(px + 50 + successZoneStart, py + 120, successZoneWidth, 50);

        // Yellow good zone (near edges)
        int goodZoneWidth = 30;
        img.setColor(new Color(255, 200, 100, 150));
        img.fillRect(px + 50 + successZoneStart - goodZoneWidth, py + 120, goodZoneWidth, 50);
        img.fillRect(px + 50 + successZoneStart + successZoneWidth, py + 120, goodZoneWidth, 50);

        // Target zone glow indicator
        int glowIntensity = 100 + (int)(80 * Math.sin(animTick * 0.15));
        img.setColor(new Color(255, 150, 150, glowIntensity));
        img.fillRect(px + 50 + successZoneStart - 5, py + 115, successZoneWidth + 10, 60);

        // Current indicator (moving)
        Color indColor = failCooldown > 0 ? new Color(255, 100, 100) : new Color(255, 200, 50);
        if (failCooldown > 20) indColor = new Color(255, 50, 50);
        img.setColor(indColor);
        img.fillRect(px + 50 + indicatorPos - 5, py + 110, 10, 70);
        
        // Indicator glow
        int indGlow = 150 + (int)(100 * Math.sin(animTick * 0.2));
        img.setColor(new Color(255, 150, 150, indGlow / 2));
        img.fillRect(px + 50 + indicatorPos - 10, py + 105, 20, 80);

        // Status and scoring
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 22));
        img.drawString("REUȘITE: " + hitCount + " / " + targetHits, px + 130, py + 205);

        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Scor: " + totalScore, px + 175, py + 235);

        if (combo > 1)
        {
            img.setColor(new Color(100, 255, 100));
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.drawString("Combo x" + combo, px + 180, py + 260);
        }

        myOverlay.setImage(img);
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
        
        if (success)
        {
            img.setColor(new Color(200, 0, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("SUCCES!", panelW / 2 - 100, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.setColor(new Color(255, 200, 100));
            img.drawString("Scor final: " + totalScore, panelW / 2 - 100, panelH / 2 + 15);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Lovituri perfecte: " + hitCount + " | Combo: x" + combo, panelW / 2 - 150, panelH / 2 + 50);
        }
        else
        {
            img.setColor(new Color(200, 0, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("EȘUAT!", panelW / 2 - 90, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.setColor(new Color(255, 100, 100));
            img.drawString("Încearcă din nou!", panelW / 2 - 90, panelH / 2 + 10);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 14));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Finalizat: " + hitCount + " / " + targetHits, panelW / 2 - 95, panelH / 2 + 50);
            img.drawString("Scor: " + totalScore, panelW / 2 - 70, panelH / 2 + 70);
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
        img.setColor(new Color(255, 120, 120, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: ELIBERARE PE RITM", 60, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Apasă SPATIU când indicatorul intră în zona verde.", 40, 70);
        img.drawString("Scop: " + targetHits + " reușite.", 160, 95);
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
    
    public java.util.List<TiledMap.CollisionRect> getCollisionRects()
    {
        return java.util.Collections.emptyList();
    }
}
