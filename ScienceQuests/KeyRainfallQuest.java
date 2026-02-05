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
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean upDown = false;
    private boolean downDown = false;
    private boolean leftDown = false;
    private boolean rightDown = false;
    private boolean tutorialActive = false;
    
    public KeyRainfallQuest(int mapX, int mapY)
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
            }
            else if (tutorialActive)
            {
                tutorialActive = false;
                clearOverlay();
            }
            startKeyDown = startPressed;
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            
            // Difficulty progression - spawn rate increases
            currentSpawnRate = Math.max(15, baseSpawnRate - (catchCount * 3));
            
            boolean upPressed = Greenfoot.isKeyDown("up");
            boolean downPressed = Greenfoot.isKeyDown("down");
            boolean leftPressed = Greenfoot.isKeyDown("left");
            boolean rightPressed = Greenfoot.isKeyDown("right");
            boolean upJust = upPressed && !upDown;
            boolean downJust = downPressed && !downDown;
            boolean leftJust = leftPressed && !leftDown;
            boolean rightJust = rightPressed && !rightDown;
            
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
                boolean caught = ("up".equals(key.keyName) && upJust) ||
                                 ("down".equals(key.keyName) && downJust) ||
                                 ("left".equals(key.keyName) && leftJust) ||
                                 ("right".equals(key.keyName) && rightJust);
                if (caught)
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
            
            upDown = upPressed;
            downDown = downPressed;
            leftDown = leftPressed;
            rightDown = rightPressed;
            
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
            
                upDown = Greenfoot.isKeyDown("up");
                downDown = Greenfoot.isKeyDown("down");
                leftDown = Greenfoot.isKeyDown("left");
                rightDown = Greenfoot.isKeyDown("right");
        World world = getWorld();
        int w = world != null ? world.getWidth() : 800;
        int h = world != null ? world.getHeight() : 600;
        
        if (world != null)
        {
            if (myOverlay == null || myOverlay.getWorld() == null)
            {
                myOverlay = new OverlayLayer();
                world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
            }
        }
        
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
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("PLAOAIA DE SĂGEȚI", px + 70, py + 50);

        // Difficulty level and speed
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 14));
        int diffLevel = Math.min(8, (catchCount / 2) + 1);
        img.drawString("INSTRUCȚIUNI: prinde săgețile în zona albastră", px + 45, py + 75);
        img.drawString("NIVEL " + diffLevel + " | RITM: " + (baseSpawnRate - currentSpawnRate) + "%", px + 125, py + 95);

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
        img.drawString("ZONA DE PRINDERE", zoneX + 60, zoneY + 35);
        
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
        img.drawString("PRINSE: " + catchCount + " / " + targetCount, px + 150, py + 145);

        // Score and combo display
        img.setColor(new Color(100, 255, 200));
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString("Scor: " + totalScore, px + 180, py + 260);

        if (combo > 1)
        {
            img.setColor(new Color(100, 255, 100));
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
            img.drawString("Combo x" + combo, px + 190, py + 235);
        }

        setImage(img);
        if (myOverlay != null)
        {
            myOverlay.setImage(img);
        }
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
            img.setColor(new Color(100, 200, 255, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 200, 255, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("AI PRINS TOT!", panelW / 2 - 125, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.setColor(new Color(150, 255, 255));
            img.drawString("Scor: " + totalScore, panelW / 2 - 70, panelH / 2 + 15);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Săgeți prinse: " + catchCount, panelW / 2 - 105, panelH / 2 + 50);
            img.drawString("Combo maxim: x" + combo, panelW / 2 - 105, panelH / 2 + 70);
        }
        else
        {
            img.setColor(new Color(100, 200, 255, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("RATAT!", panelW / 2 - 90, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.setColor(new Color(255, 150, 150));
            img.drawString("O săgeată a scăpat!", panelW / 2 - 120, panelH / 2 + 10);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 15));
            img.setColor(new Color(200, 200, 200));
            img.drawString("Prinse: " + catchCount + " / " + targetCount, panelW / 2 - 95, panelH / 2 + 50);
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
        img.setColor(new Color(120, 200, 255, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: PLAOAIA DE SĂGEȚI", 70, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Apasă săgeata potrivită când trece prin zona albastră.", 25, 70);
        img.drawString("Scop: prinde " + targetCount + " săgeți.", 140, 95);
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
