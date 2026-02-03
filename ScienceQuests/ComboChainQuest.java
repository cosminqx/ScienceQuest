import greenfoot.*;

/**
 * ComboChainQuest - Complete key combinations (SPACE+arrow) with stylish UI
 */
public class ComboChainQuest extends Actor
{
    private int mapX, mapY;
    private int comboStep = 0;
    private String[] combos = {"up", "down", "left", "right"};
    private String[] comboArrows = {"▲", "▼", "◀", "▶"};
    private boolean spaceHeld = false;
    private int timeRemaining = 300;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int successTick = 0;
    private int failTick = 0;
    
    public ComboChainQuest(int mapX, int mapY)
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
            img.setColor(Color.WHITE);
            img.drawString("⚙", 15, 30);
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
                comboStep = 0;
                animTick = 0;
                successTick = 0;
                failTick = 0;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        
        if (questActive)
        {
            animTick++;
            spaceHeld = Greenfoot.isKeyDown("space");
            
            if (spaceHeld && Greenfoot.isKeyDown(combos[comboStep]))
            {
                successTick = 20;
                comboStep++;
                if (comboStep >= combos.length)
                {
                    finishQuest(true);
                }
            }
            
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
        
        // Get or create overlay layer
        java.util.List<OverlayLayer> overlays = world.getObjects(OverlayLayer.class);
        OverlayLayer overlay;
        if (overlays.isEmpty()) {
            overlay = new OverlayLayer();
            world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        } else {
            overlay = overlays.get(0);
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
        img.drawString("COMBO CHAIN", px + 110, py + 50);

        // Instruction
        img.setFont(new greenfoot.Font("Arial", true, false, 16));
        img.setColor(new Color(200, 200, 255));
        img.drawString("Press SPACE + Direction Arrow", px + 95, py + 80);

        // Current combo instruction
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        Color instrColor = spaceHeld ? new Color(100, 255, 100) : new Color(255, 200, 100);
        img.setColor(instrColor);
        img.drawString("SPACE + " + comboArrows[comboStep], px + 130, py + 130);

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
        img.drawString("Step: " + comboStep + " / " + combos.length, px + 155, py + 240);

        // Time remaining
        img.setColor(new Color(100, 200, 255));
        img.drawString("Time: " + (timeRemaining / 60 + 1) + "s", px + 320, py + 240);

        setImage(img);
        overlay.setImage(img);
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
        int score = comboStep * 150;
        
        if (success)
        {
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);
            
            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 40));
            img.drawString("SUCCESS!", panelW / 2 - 120, panelH / 2 - 30);
            img.setFont(new greenfoot.Font("Arial", true, false, 18));
            img.drawString("All combos completed!", panelW / 2 - 110, panelH / 2 + 30);
            img.drawString("Score: " + score, panelW / 2 - 60, panelH / 2 + 60);
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
            img.setFont(new greenfoot.Font("Arial", true, false, 16));
            img.drawString("Completed " + comboStep + "/" + combos.length, panelW / 2 - 90, panelH / 2 + 30);
            img.drawString("Score: " + score, panelW / 2 - 60, panelH / 2 + 60);
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
