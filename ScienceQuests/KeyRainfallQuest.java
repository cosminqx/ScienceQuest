import greenfoot.*;
import java.util.*;

/**
 * KeyRainfallQuest - Catch falling keys with speed progression and depth scaling
 * Features: Cyan glowing aura, falling animations, catch zone visualization, difficulty escalation
 */
public class KeyRainfallQuest extends Actor
{
    private int mapX, mapY;
    private int catchCount = 0;
    private int targetCount = 8;
    private List<FallingKey> fallingKeys = new ArrayList<>();
    private int spawnTimer = 0;
    private int totalScore = 0;
    private int combo = 0;
    private int baseSpawnRate = 45;
    private int currentSpawnRate;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    
    public KeyRainfallQuest(int mapX, int mapY)
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
                catchCount = 0;
                totalScore = 0;
                combo = 0;
                fallingKeys.clear();
                spawnTimer = 0;
                currentSpawnRate = baseSpawnRate;
                animTick = 0;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            
            // Difficulty progression - spawn rate increases
            currentSpawnRate = Math.max(15, baseSpawnRate - (catchCount * 3));
            
            // Spawn new keys
            spawnTimer++;
            if (spawnTimer > currentSpawnRate && catchCount < targetCount)
            {
                spawnKey();
                spawnTimer = 0;
            }
            
            // Update falling keys and check catches
            for (int i = fallingKeys.size() - 1; i >= 0; i--)
            {
                FallingKey key = fallingKeys.get(i);
                key.fall();
                
                // Check if key was caught
                if (Greenfoot.isKeyDown(key.keyName))
                {
                    // Score based on how early caught (earlier = more points)
                    int catchZoneDist = Math.abs(key.y - 150);
                    int catchBonus = Math.max(50, 300 - (catchZoneDist * 2));
                    int points = 200 + catchBonus + (combo * 25);
                    totalScore += points;
                    
                    catchCount++;
                    combo++;
                    fallingKeys.remove(i);
                    
                    if (catchCount >= targetCount)
                    {
                        finishQuest(true);
                    }
                }
                // Check if key reached bottom (missed)
                else if (key.y > 200)
                {
                    combo = 0;
                    fallingKeys.remove(i);
                }
            }
            
            updateDisplay();
        }
    }
    
    private void spawnKey()
    {
        String[] keys = {"up", "down", "left", "right"};
        String key = keys[Greenfoot.getRandomNumber(4)];
        fallingKeys.add(new FallingKey(key, 80 + Greenfoot.getRandomNumber(220)));
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

        // Glowing aura effect (cyan: 100, 200, 255)
        img.setColor(new Color(100, 200, 255, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel background
        img.setColor(new Color(10, 20, 35, 220));
        img.fillRect(px, py, panelW, panelH);

        // Double-line fancy borders with cyan glow
        img.setColor(new Color(100, 200, 255, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(150, 220, 255, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("KEY RAINFALL", px + 95, py + 50);

        // Difficulty level and speed
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        int diffLevel = Math.min(8, (catchCount / 2) + 1);
        img.drawString("LEVEL " + diffLevel + " | SPAWN RATE: " + (baseSpawnRate - currentSpawnRate) + "%", px + 95, py + 75);

        // Catch zone visualization at bottom
        int zoneX = px + 80;
        int zoneY = py + 180;
        int zoneW = 300;
        int zoneH = 50;
        
        // Zone background
        img.setColor(new Color(50, 100, 150, 150));
        img.fillRect(zoneX, zoneY, zoneW, zoneH);
        
        // Zone label
        img.setColor(new Color(150, 220, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.drawString("CATCH ZONE", zoneX + 100, zoneY + 35);
        
        // Pulsing zone glow
        int zoneGlow = 100 + (int)(80 * Math.sin(animTick * 0.15));
        img.setColor(new Color(100, 200, 255, zoneGlow / 2));
        img.fillRect(zoneX - 4, zoneY - 4, zoneW + 8, zoneH + 8);
        
        // Zone border
        img.setColor(new Color(100, 200, 255, 200));
        img.drawRect(zoneX, zoneY, zoneW, zoneH);

        // Falling keys rendering
        for (FallingKey key : fallingKeys)
        {
            // Scale keys based on fall progress (depth effect)
            float scale = 0.7f + (key.y / 200.0f) * 0.3f;
            int keySize = (int)(20 * scale);
            
            // Alpha fading as they approach catch zone
            int alpha = 255;
            if (key.y > 150)
            {
                alpha = 150 + (int)((200 - key.y) * 1.05f);
            }
            
            img.setColor(new Color(100, 255, 200, alpha));
            
            String arrow = key.keyName.equals("up") ? "▲" :
                          key.keyName.equals("down") ? "▼" :
                          key.keyName.equals("left") ? "◀" : "▶";
            
            img.setFont(new greenfoot.Font("Arial", true, false, keySize));
            int keyX = px + key.x - (int)(keySize / 2.5);
            int keyY = py + key.y + 45;
            img.drawString(arrow, keyX, keyY);
            
            // Glow effect on keys
            img.setColor(new Color(100, 255, 200, Math.max(0, alpha / 3)));
            img.drawString(arrow, keyX - 3, keyY - 3);
        }

        // Progress tracking
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 22));
        img.drawString("CAUGHT: " + catchCount + " / " + targetCount, px + 135, py + 145);

        // Score and combo display
        img.setColor(new Color(100, 255, 200));
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString("Score: " + totalScore, px + 170, py + 260);

        if (combo > 1)
        {
            img.setColor(new Color(100, 255, 100));
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
            img.drawString("Combo x" + combo, px + 190, py + 235);
        }

        setImage(img);
    }
    
    private void finishQuest(boolean success)
    {
        questActive = false;
        completed = true;
        GameState.getInstance().setMiniQuestActive(false);

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
            img.setColor(new Color(100, 200, 255, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 200, 255, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("CAUGHT ALL!", panelW / 2 - 130, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.setColor(new Color(150, 255, 255));
            img.drawString("Score: " + totalScore, panelW / 2 - 80, panelH / 2 + 15);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Keys Caught: " + catchCount, panelW / 2 - 85, panelH / 2 + 50);
            img.drawString("Best Combo: x" + combo, panelW / 2 - 95, panelH / 2 + 70);
        }
        else
        {
            img.setColor(new Color(100, 200, 255, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("MISSED!", panelW / 2 - 100, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.setColor(new Color(255, 150, 150));
            img.drawString("A key slipped away!", panelW / 2 - 110, panelH / 2 + 10);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Caught: " + catchCount + " / " + targetCount, panelW / 2 - 95, panelH / 2 + 50);
            img.drawString("Score: " + totalScore, panelW / 2 - 70, panelH / 2 + 70);
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
    
    private class FallingKey
    {
        String keyName;
        int x;
        int y = 0;
        
        FallingKey(String key, int xPos)
        {
            this.keyName = key;
            this.x = xPos;
        }
        
        void fall()
        {
            // Speed increases over time (difficulty progression)
            int speed = 2 + (catchCount / 3);
            y += speed;
        }
    }
    
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }
    
    public java.util.List<TiledMap.CollisionRect> getCollisionRects()
    {
        return java.util.Collections.emptyList();
    }
}
