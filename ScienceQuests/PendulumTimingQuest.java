import greenfoot.*;

/**
 * PendulumTimingQuest - PHYSICS: Release SPACE when pendulum reaches center
 */
public class PendulumTimingQuest extends Actor
{
    private int mapX, mapY;
    private int successfulReleases = 0;
    private int targetReleases = 3;
    private int timeRemaining = 900; // 15 seconds
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private boolean wasSpacePressed = false;
    private double pendulumAngle = -45; // -45 to 45 degrees
    private double pendulumVelocity = 1.5;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    
    public PendulumTimingQuest(int mapX, int mapY)
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
            if (distance < 100 && interactionCooldown == 0 && startPressed && !startKeyDown)
            {
                questActive = true;
                animTick = 0;
                successfulReleases = 0;
                pendulumAngle = -45;
                pendulumVelocity = 1.5;
                timeRemaining = 900;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
            startKeyDown = startPressed;
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            
            // Update pendulum physics
            pendulumAngle += pendulumVelocity;
            if (pendulumAngle > 45) {
                pendulumAngle = 45;
                pendulumVelocity = -pendulumVelocity;
            } else if (pendulumAngle < -45) {
                pendulumAngle = -45;
                pendulumVelocity = -pendulumVelocity;
            }
            
            // Check for release timing
            boolean spacePressed = Greenfoot.isKeyDown("space");
            if (!spacePressed && wasSpacePressed)
            {
                // Player released space - check timing
                if (Math.abs(pendulumAngle) < 5) // Within 5 degrees of center
                {
                    successfulReleases++;
                    if (successfulReleases >= targetReleases)
                    {
                        finishQuest(true);
                    }
                }
            }
            wasSpacePressed = spacePressed;
            
            timeRemaining--;
            updateDisplay();
            
            if (timeRemaining <= 0)
            {
                finishQuest(false);
            }
        }
    }
    
    private void updateDisplay()
    {
        World world = getWorld();
        if (world == null) return;
        
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
        }
        
        int panelW = 460;
        int panelH = 300;
        GreenfootImage img = new GreenfootImage(panelW, panelH);

        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        // Blue physics theme glow
        img.setColor(new Color(80, 150, 255, 60));
        img.fillRect(-8, -8, panelW + 16, panelH + 16);

        img.setColor(new Color(10, 10, 30, 245));
        img.fillRect(0, 0, panelW, panelH);

        img.setColor(new Color(80, 150, 255, 220));
        img.drawRect(0, 0, panelW, panelH);
        img.setColor(new Color(100, 170, 255, 120));
        img.drawRect(1, 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 26));
        img.drawString("TIMINGUL PENDULULUI", 80, 40);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(150, 200, 255));
        img.drawString("INSTRUCȚIUNI: eliberează SPATIU în centru", 70, 65);

        // Draw pendulum
        int pivotX = panelW / 2;
        int pivotY = 100;
        int pendulumLength = 80;
        double radians = Math.toRadians(pendulumAngle);
        int bobX = pivotX + (int)(pendulumLength * Math.sin(radians));
        int bobY = pivotY + (int)(pendulumLength * Math.cos(radians));
        
        // Pivot point
        img.setColor(new Color(200, 200, 200));
        img.fillOval(pivotX - 5, pivotY - 5, 10, 10);
        
        // Pendulum string
        img.setColor(new Color(150, 150, 150));
        img.drawLine(pivotX, pivotY, bobX, bobY);
        
        // Center zone (green = perfect timing)
        boolean inCenterZone = Math.abs(pendulumAngle) < 5;
        img.setColor(inCenterZone ? new Color(100, 255, 100, 100) : new Color(100, 255, 100, 30));
        img.fillRect(pivotX - 15, pivotY, 30, pendulumLength + 20);
        img.setColor(new Color(100, 255, 100));
        img.drawRect(pivotX - 15, pivotY, 30, pendulumLength + 20);
        
        // Pendulum bob
        img.setColor(inCenterZone ? new Color(100, 255, 100) : new Color(255, 200, 100));
        img.fillOval(bobX - 12, bobY - 12, 24, 24);
        img.setColor(Color.WHITE);
        img.drawOval(bobX - 12, bobY - 12, 24, 24);

        // Stats
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 255, 255));
        img.drawString("Eliberări corecte: " + successfulReleases + " / " + targetReleases, 90, 220);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(150, 200, 255));
        img.drawString("Timp: " + (timeRemaining / 60 + 1) + "s", 190, 245);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 12));
        img.setColor(inCenterZone ? new Color(100, 255, 100) : new Color(255, 200, 100));
        img.drawString(inCenterZone ? "RELEASE NOW!" : "Wait for center...", 165, 270);

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
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("SUCCES!", panelW / 2 - 100, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("Timing perfect! Mișcarea periodică", panelW / 2 - 150, panelH / 2 + 30);
            img.drawString("este înțeleasă.", panelW / 2 - 85, panelH / 2 + 55);
        }
        else
        {
            img.setColor(new Color(200, 0, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("EȘUAT!", panelW / 2 - 90, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("Eliberare greșită. Recitește", panelW / 2 - 135, panelH / 2 + 30);
            img.drawString("mișcarea armonică. " + successfulReleases + "/" + targetReleases + " finalizat.", panelW / 2 - 150, panelH / 2 + 55);
        }
        
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
