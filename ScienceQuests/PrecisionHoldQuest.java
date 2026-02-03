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
    private int levelCount = 0;
    private int totalScore = 0;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int successFlash = 0;
    
    public PrecisionHoldQuest(int mapX, int mapY)
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
        img.setColor(new Color(200, 100, 220));
        img.drawString("◀", 15, 30);
        setImage(img);
    }
    
    public void act()
    {
        if (completed) return;
        
        // Check for player proximity and SPACE key to start
        Actor player = getPlayer();
        if (player != null && !questActive)
        {
            int dx = Math.abs(player.getX() - getX());
            int dy = Math.abs(player.getY() - getY());
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < 100 && interactionCooldown == 0 && Greenfoot.isKeyDown("space"))
            {
                questActive = true;
                holdTime = 0;
                animTick = 0;
                successFlash = 0;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        if (successFlash > 0) successFlash--;
        
        if (questActive)
        {
            animTick++;
            if (Greenfoot.isKeyDown("left"))
            {
                holdTime++;
                
                // Perfect zone pulse feedback
                if (holdTime >= (targetTime - tolerance) && holdTime <= (targetTime + tolerance))
                {
                    successFlash = 5;
                }
            }
            else
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
            
            holdTime = 0;
            questActive = false;
            completed = true;
            
            GameState.getInstance().setMiniQuestActive(false);
            finishQuest(true);
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
        int w = world != null ? world.getWidth() : 800;
        int h = world != null ? world.getHeight() : 600;
        GreenfootImage img = new GreenfootImage(w, h);

        // Animated pulsing background
        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, w, h);

        int panelW = 460;
        int panelH = 280;
        int px = (w - panelW) / 2;
        int py = (h - panelH) / 2;

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
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("PRECISION HOLD", px + 85, py + 50);

        // Difficulty level
        img.setColor(new Color(200, 150, 220));
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        img.drawString("LEVEL " + (levelCount + 1) + " | TARGET: " + (targetTime / 60.0f) + "s", px + 125, py + 75);

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
        img.drawString("SHORT", barX + 5, barY - 5);
        img.drawString("PERFECT", barX + progressBarWidth / 2 - 25, barY - 5);
        img.drawString("LONG", barX + progressBarWidth - 40, barY - 5);

        // Time display
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        float seconds = holdTime / 60.0f;
        float targetSeconds = targetTime / 60.0f;
        img.drawString(String.format("HELD: %.2f / %.2f sec", seconds, targetSeconds), px + 110, py + 220);

        // Score display
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString("Score: " + totalScore, px + 170, py + 250);

        setImage(img);
    }
    
    private void finishQuest(boolean success)
    {
        World world = getWorld();
        if (world == null) return;
        
        // Get overlay layer
        java.util.List<OverlayLayer> overlays = world.getObjects(OverlayLayer.class);
        if (overlays.isEmpty()) return;
        OverlayLayer overlay = overlays.get(0);
        
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
            img.drawString("PERFECT!", panelW / 2 - 110, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.setColor(new Color(100, 255, 200));
            img.drawString("Score: " + totalScore, panelW / 2 - 80, panelH / 2 + 15);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Hold Time: " + String.format("%.2f sec", holdTime / 60.0f), panelW / 2 - 110, panelH / 2 + 50);
        }
        else
        {
            img.setColor(new Color(180, 100, 220, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(200, 100, 255, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("FAILED!", panelW / 2 - 100, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.setColor(new Color(255, 150, 200));
            img.drawString("Hold too " + (holdTime < targetTime ? "SHORT" : "LONG"), panelW / 2 - 120, panelH / 2 + 10);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 14));
            img.setColor(new Color(200, 200, 200));
            float held = holdTime / 60.0f;
            float target = targetTime / 60.0f;
            img.drawString("You: " + String.format("%.2f sec", held), panelW / 2 - 80, panelH / 2 + 45);
            img.drawString("Target: " + String.format("%.2f sec", target), panelW / 2 - 95, panelH / 2 + 65);
        }
        
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
    
    public java.util.List<TiledMap.CollisionRect> getCollisionRects()
    {
        return java.util.Collections.emptyList();
    }
}
