import greenfoot.*;

/**
 * ComboChainQuest - Complete key combinations (SPACE+arrow) with stylish UI
 */
public class ComboChainQuest extends Actor
{
    private int mapX, mapY;
    private int comboStep = 0;
    private String[] combos = {"up", "down", "left", "right"};
    private String[] comboArrows = {"↑", "↓", "←", "→"};
    private boolean spaceHeld = false;
    private int timeRemaining = 300;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int successTick = 0;
    private int failTick = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean comboKeyDown = false;
    private boolean tutorialActive = false;
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    
    public ComboChainQuest(int mapX, int mapY)
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
                        comboStep = 0;
                        animTick = 0;
                        successTick = 0;
                        failTick = 0;
                        comboKeyDown = false;
                        timeRemaining = 300;
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
            spaceHeld = Greenfoot.isKeyDown("space");
            boolean comboPressed = Greenfoot.isKeyDown(combos[comboStep]);
            
            if (spaceHeld && comboPressed && !comboKeyDown)
            {
                successTick = 20;
                comboStep++;
                if (comboStep >= combos.length)
                {
                    finishQuest(true);
                }
            }
            comboKeyDown = comboPressed;
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

        int pulse = 90 + (int)(70 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        int px = 0;
        int py = 0;

        // Glowing aura effect
        img.setColor(new Color(100, 200, 255, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel background
        img.setColor(new Color(10, 10, 30, 245));
        img.fillRect(px, py, panelW, panelH);

        // Fancy double border with glow
        img.setColor(new Color(100, 200, 255, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(120, 220, 255, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("LANȚ COMBO", px + 135, py + 50);

        // Instruction
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.setColor(new Color(200, 200, 255));
        img.drawString("INSTRUCȚIUNI: ține SPATIU și apasă săgeata", px + 45, py + 80);

        // Current combo instruction
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        Color instrColor = spaceHeld ? new Color(100, 255, 100) : new Color(255, 200, 100);
        img.setColor(instrColor);
        int safeStep = Math.min(comboStep, comboArrows.length - 1);
        img.drawString("SPATIU + " + comboArrows[safeStep], px + 130, py + 130);

        // Combo step indicators with glow
        for (int i = 0; i < combos.length; i++)
        {
            int boxX = px + 100 + i * 90;
            int boxY = py + 170;
            
            if (i < comboStep)
            {
                // Completed step - green glow
                img.setColor(new Color(100, 255, 100, 80));
                img.fillRect(boxX - 2, boxY - 2, 64, 34);
                img.setColor(new Color(100, 255, 100, 200));
            }
            else if (i == comboStep)
            {
                // Current step - blue pulse glow
                int glow = 100 + (int)(50 * Math.sin(animTick * 0.15));
                img.setColor(new Color(100, 200, 255, glow));
                img.fillRect(boxX - 2, boxY - 2, 64, 34);
                img.setColor(new Color(100, 220, 255, 220));
            }
            else
            {
                // Future step - dim gray
                img.setColor(new Color(80, 80, 100, 80));
                img.fillRect(boxX - 2, boxY - 2, 64, 34);
                img.setColor(new Color(100, 100, 120, 120));
            }
            
            img.drawRect(boxX - 2, boxY - 2, 64, 34);
            img.setColor(Color.WHITE);
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.drawString(comboArrows[i], boxX + 18, boxY + 18);
        }

        // Progress text
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 200, 100));
        img.drawString("Pas: " + comboStep + " / " + combos.length, px + 175, py + 240);

        // Time remaining
        img.setColor(new Color(100, 200, 255));
        img.drawString("Timp: " + (timeRemaining / 60 + 1) + "s", px + 320, py + 240);

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
        int score = comboStep * 150;
        
        if (success)
        {
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
               img.drawString("SUCCES!", panelW / 2 - 100, panelH / 2 - 30);
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
               img.drawString("Toate combo-urile completate!", panelW / 2 - 145, panelH / 2 + 30);
            img.drawString("Scor: " + score, panelW / 2 - 60, panelH / 2 + 60);
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
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
               img.drawString("Finalizat " + comboStep + "/" + combos.length, panelW / 2 - 90, panelH / 2 + 30);
            img.drawString("Scor: " + score, panelW / 2 - 60, panelH / 2 + 60);
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
        img.setColor(new Color(100, 170, 255, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: COMBO", 145, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Ține SPATIU și apasă săgeata indicată.", 85, 70);
        img.drawString("Scop: completează toate combo‑urile.", 105, 95);
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
