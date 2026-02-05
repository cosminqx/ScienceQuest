import greenfoot.*;

/**
 * ChemicalBondQuest - CHEMISTRY: Hold two arrow keys simultaneously to form molecular bonds
 */
public class ChemicalBondQuest extends Actor
{
    private int mapX, mapY;
    private int bondsFormed = 0;
    private int targetBonds = 5;
    private int timeRemaining = 600; // 10 seconds
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private String[] requiredBonds = {"up+right", "down+left", "up+left", "down+right", "left+right"};
    private int currentBondIndex = 0;
    private int bondHoldTime = 0;
    private int bondFormTick = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean tutorialActive = false;
    
    public ChemicalBondQuest(int mapX, int mapY)
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
                        animTick = 0;
                        bondsFormed = 0;
                        currentBondIndex = 0;
                        bondHoldTime = 0;
                        timeRemaining = 600;
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
            if (bondFormTick > 0) bondFormTick--;
            
            checkBondFormation();
            if (!questActive) return;
            
            timeRemaining--;
            updateDisplay();
            
            if (timeRemaining <= 0)
            {
                finishQuest(false);
            }
        }
    }

    public boolean isCompleted()
    {
        return completed;
    }
    
    private void checkBondFormation()
    {
        int safeIndex = Math.min(currentBondIndex, requiredBonds.length - 1);
        String required = requiredBonds[safeIndex];
        String[] keys = required.split("\\+");
        
        boolean allPressed = true;
        for (String key : keys)
        {
            if (!Greenfoot.isKeyDown(key.trim()))
            {
                allPressed = false;
                break;
            }
        }
        
        if (allPressed)
        {
            bondHoldTime++;
            if (bondHoldTime >= 30) // Hold for 0.5 seconds
            {
                bondsFormed++;
                currentBondIndex++;
                bondHoldTime = 0;
                bondFormTick = 20;
                
                if (bondsFormed >= targetBonds)
                {
                    finishQuest(true);
                }
            }
        }
        else
        {
            bondHoldTime = 0;
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

        // Green chemistry theme glow
        img.setColor(new Color(100, 255, 150, 60));
        img.fillRect(-8, -8, panelW + 16, panelH + 16);

        img.setColor(new Color(10, 30, 10, 245));
        img.fillRect(0, 0, panelW, panelH);

        img.setColor(new Color(100, 255, 150, 220));
        img.drawRect(0, 0, panelW, panelH);
        img.setColor(new Color(120, 255, 170, 120));
        img.drawRect(1, 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 26));
        img.drawString("LEGĂTURI CHIMICE", 110, 40);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(150, 255, 200));
        img.drawString("INSTRUCȚIUNI: ține două săgeți simultan", 70, 65);

        // Current required bond
        String required = requiredBonds[currentBondIndex];
        String[] keys = required.split("\\+");
        
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.setColor(new Color(255, 255, 255));
        img.drawString("Formează legătura:", 130, 110);
        
        // Draw atom representations with bond
        int atomY = 150;
        int atom1X = panelW / 2 - 60;
        int atom2X = panelW / 2 + 60;
        
        // Check if keys are pressed
        boolean key1Pressed = Greenfoot.isKeyDown(keys[0].trim());
        boolean key2Pressed = Greenfoot.isKeyDown(keys[1].trim());
        boolean bothPressed = key1Pressed && key2Pressed;
        
        // Atom 1
        Color atom1Color = key1Pressed ? new Color(100, 255, 100) : new Color(150, 150, 150);
        img.setColor(atom1Color);
        img.fillOval(atom1X - 25, atomY - 25, 50, 50);
        img.setColor(Color.WHITE);
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.drawString(keys[0].trim().toUpperCase(), atom1X - 12, atomY + 5);
        
        // Bond line
        if (bothPressed)
        {
            float progress = bondHoldTime / 30.0f;
            img.setColor(new Color(100, 255, 100, (int)(200 * progress)));
            for (int i = 0; i < 3; i++)
            {
                img.drawLine(atom1X + 25, atomY + i - 1, atom2X - 25, atomY + i - 1);
            }
        }
        else
        {
            img.setColor(new Color(100, 100, 100, 100));
            img.drawLine(atom1X + 25, atomY, atom2X - 25, atomY);
        }
        
        // Atom 2
        Color atom2Color = key2Pressed ? new Color(100, 255, 100) : new Color(150, 150, 150);
        img.setColor(atom2Color);
        img.fillOval(atom2X - 25, atomY - 25, 50, 50);
        img.setColor(Color.WHITE);
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.drawString(keys[1].trim().toUpperCase(), atom2X - 12, atomY + 5);

        // Progress bar for hold time
        if (bondHoldTime > 0)
        {
            float progress = bondHoldTime / 30.0f;
            int barW = (int)(panelW - 80) * (int)progress / 100;
            img.setColor(new Color(100, 255, 100, 100));
            img.fillRect(40, 210, (int)((panelW - 80) * progress), 15);
            img.setColor(new Color(100, 255, 100));
            img.drawRect(40, 210, panelW - 80, 15);
            
            img.setFont(new greenfoot.Font("Arial", false, false, 12));
            img.setColor(Color.WHITE);
            img.drawString("Ține apăsat pentru legătură...", 115, 223);
        }

        // Stats
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 255, 255));
        img.drawString("Legături formate: " + bondsFormed + " / " + targetBonds, 105, 250);
        
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(150, 255, 200));
        img.drawString("Timp: " + (timeRemaining / 60 + 1) + "s", 190, 275);

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
            img.drawString("Molecule stabile formate!", panelW / 2 - 120, panelH / 2 + 30);
            img.drawString("Legătura covalentă e înțeleasă.", panelW / 2 - 150, panelH / 2 + 55);
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
            img.drawString("Legături instabile. Recitește", panelW / 2 - 140, panelH / 2 + 30);
            img.drawString("partajarea electronilor. " + bondsFormed + "/" + targetBonds + " finalizat.", panelW / 2 - 165, panelH / 2 + 55);
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
        img.setColor(new Color(120, 255, 170, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: LEGĂTURI", 135, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Ține simultan cele două săgeți afișate.", 85, 70);
        img.drawString("Scop: " + targetBonds + " legături formate.", 125, 95);
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
