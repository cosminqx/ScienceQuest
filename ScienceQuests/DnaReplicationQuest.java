import greenfoot.*;

/**
 * DnaReplicationQuest - BIOLOGY: Press complementary base pairs in sequence
 */
public class DnaReplicationQuest extends Actor
{
    private int mapX, mapY;
    private int strandsCompleted = 0;
    private int targetStrands = 3;
    private int timeRemaining = 720; // 12 seconds
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    
    private String[] bases = {"A", "T", "G", "C", "T", "A", "G", "C"};
    private int currentBaseIndex = 0;
    private int correctFeedbackTick = 0;
    private int wrongFeedbackTick = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean anyArrowDown = false;
    
    public DnaReplicationQuest(int mapX, int mapY)
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
                animTick = 0;
                strandsCompleted = 0;
                currentBaseIndex = 0;
                timeRemaining = 720;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
            startKeyDown = startPressed;
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            if (correctFeedbackTick > 0) correctFeedbackTick--;
            if (wrongFeedbackTick > 0) wrongFeedbackTick--;
            
            checkInput();
            
            timeRemaining--;
            updateDisplay();
            
            if (timeRemaining <= 0)
            {
                finishQuest(false);
            }
        }
    }
    
    private void checkInput()
    {
        String currentBase = bases[currentBaseIndex];
        String complement = getComplement(currentBase);
        
        // Map: A=up, T=down, G=left, C=right
        boolean up = Greenfoot.isKeyDown("up");
        boolean down = Greenfoot.isKeyDown("down");
        boolean left = Greenfoot.isKeyDown("left");
        boolean right = Greenfoot.isKeyDown("right");
        boolean anyDownNow = up || down || left || right;
        boolean expectedPressed = Greenfoot.isKeyDown(getKeyForBase(complement));
        
        if (anyDownNow && !anyArrowDown)
        {
            if (expectedPressed)
            {
                correctFeedbackTick = 15;
                currentBaseIndex++;
                
                if (currentBaseIndex >= bases.length)
                {
                    strandsCompleted++;
                    currentBaseIndex = 0;
                    
                    if (strandsCompleted >= targetStrands)
                    {
                        finishQuest(true);
                    }
                }
            }
            else
            {
                wrongFeedbackTick = 15;
            }
        }
        
        anyArrowDown = anyDownNow;
    }
    
    private String getComplement(String base)
    {
        switch(base)
        {
            case "A": return "T";
            case "T": return "A";
            case "G": return "C";
            case "C": return "G";
            default: return "";
        }
    }
    
    private String getKeyForBase(String base)
    {
        switch(base)
        {
            case "A": return "up";
            case "T": return "down";
            case "G": return "left";
            case "C": return "right";
            default: return "";
        }
    }
    
    private String getArrowForBase(String base)
    {
        switch(base)
        {
            case "A": return "▲";
            case "T": return "▼";
            case "G": return "◀";
            case "C": return "▶";
            default: return "";
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
        int panelH = 320;
        GreenfootImage img = new GreenfootImage(panelW, panelH);

        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        // Pink/purple biology theme glow
        img.setColor(new Color(255, 100, 200, 60));
        img.fillRect(-8, -8, panelW + 16, panelH + 16);

        img.setColor(new Color(30, 10, 30, 245));
        img.fillRect(0, 0, panelW, panelH);

        img.setColor(new Color(255, 100, 200, 220));
        img.drawRect(0, 0, panelW, panelH);
        img.setColor(new Color(255, 120, 220, 120));
        img.drawRect(1, 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 26));
        img.drawString("DNA REPLICATION", 100, 40);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(255, 150, 220));
        img.drawString("BIOLOGY: Press complementary base pairs", 70, 65);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 12));
        img.drawString("A=▲  T=▼  G=◀  C=▶", 155, 85);

        // DNA strand visualization
        int strandY = 120;
        int baseSpacing = 45;
        int startX = 50;
        
        for (int i = 0; i < 8; i++)
        {
            int x = startX + i * baseSpacing;
            String base = bases[i];
            String complement = getComplement(base);
            
            boolean isCurrentBase = (i == currentBaseIndex);
            Color baseColor = i < currentBaseIndex ? new Color(100, 255, 100) : 
                             isCurrentBase ? new Color(255, 200, 100) : new Color(150, 150, 150);
            
            // Original base (top strand)
            img.setColor(baseColor);
            img.fillOval(x - 12, strandY - 12, 24, 24);
            img.setColor(Color.WHITE);
            img.setFont(new greenfoot.Font("Arial", true, false, 14));
            img.drawString(base, x - 6, strandY + 5);
            
            // Connector line
            if (i < currentBaseIndex)
            {
                img.setColor(new Color(100, 255, 100));
                img.drawLine(x, strandY + 12, x, strandY + 38);
            }
            else
            {
                img.setColor(new Color(100, 100, 100, 100));
                img.drawLine(x, strandY + 12, x, strandY + 38);
            }
            
            // Complement base (bottom strand)
            Color compColor = i < currentBaseIndex ? new Color(100, 255, 100) : new Color(80, 80, 80);
            img.setColor(compColor);
            img.fillOval(x - 12, strandY + 38, 24, 24);
            img.setColor(Color.WHITE);
            img.drawString(i < currentBaseIndex ? complement : "?", x - 6, strandY + 55);
        }

        // Current instruction
        if (currentBaseIndex < bases.length)
        {
            String currentBase = bases[currentBaseIndex];
            String complement = getComplement(currentBase);
            String arrow = getArrowForBase(complement);
            
            Color instrColor = correctFeedbackTick > 0 ? new Color(100, 255, 100) :
                              wrongFeedbackTick > 0 ? new Color(255, 100, 100) :
                              new Color(255, 200, 100);
            
            img.setFont(new greenfoot.Font("Arial", true, false, 24));
            img.setColor(instrColor);
            img.drawString("Press: " + complement + " " + arrow, 145, 220);
        }

        // Progress
        float progress = currentBaseIndex / 8.0f;
        int barW = (int)((panelW - 80) * progress);
        img.setColor(new Color(100, 255, 100, 100));
        img.fillRect(40, 250, barW, 15);
        img.setColor(new Color(255, 100, 200));
        img.drawRect(40, 250, panelW - 80, 15);

        // Stats
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 255, 255));
        img.drawString("Strands: " + strandsCompleted + "/" + targetStrands + " | Bases: " + currentBaseIndex + "/8", 85, 285);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(255, 150, 220));
        img.drawString("Time: " + (timeRemaining / 60 + 1) + "s", 190, 305);

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
            img.drawString("SUCCESS!", panelW / 2 - 120, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("DNA replicated perfectly!", panelW / 2 - 110, panelH / 2 + 30);
            img.drawString("Base pairing understood.", panelW / 2 - 105, panelH / 2 + 55);
        }
        else
        {
            img.setColor(new Color(200, 0, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(255, 100, 100, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("FAILED!", panelW / 2 - 110, panelH / 2 - 30);
            
            img.setFont(new greenfoot.Font("Arial", false, false, 16));
            img.drawString("Replication error. Review", panelW / 2 - 115, panelH / 2 + 30);
            img.drawString("complementary base rules. " + strandsCompleted + "/" + targetStrands, panelW / 2 - 140, panelH / 2 + 55);
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
