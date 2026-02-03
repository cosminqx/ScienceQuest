import greenfoot.*;

/**
 * DoubleTapSprintQuest - Double-tap SPACE with visual flash feedback, particle bursts, and glowing UI
 */
public class DoubleTapSprintQuest extends Actor
{
    private int mapX, mapY;
    private int score = 0;
    private int totalTaps = 0;
    private int successfulDoubleTaps = 0;
    private int targetDoubleTaps = 8;
    private boolean questActive = false;
    private boolean completed = false;
    private boolean success = false;
    private int interactionCooldown = 0;
    private int animTick = 0;
    private int lastSpacePress = -100;
    private int doubleTapWindow = 20;
    private int resultScreenTick = 0;
    private int lastTapFeedbackTick = 0;
    private boolean lastWasSuccess = false;
    private int particleEmitTick = 0;
    private java.util.List<Particle> particles;
    
    private class Particle
    {
        int x, y;
        int vx, vy;
        int life;
        int maxLife;
        Color color;
        
        Particle(int x, int y, int vx, int vy, Color color)
        {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLife = 30;
            this.life = 30;
        }
        
        void update()
        {
            x += vx;
            y += vy;
            vy += 1;
            life--;
        }
        
        boolean alive() { return life > 0; }
    }
    
    public DoubleTapSprintQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        particles = new java.util.ArrayList<Particle>();
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
                score = 0;
                totalTaps = 0;
                successfulDoubleTaps = 0;
                lastSpacePress = -100;
                animTick = 0;
                particles.clear();
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
        }
        
        if (interactionCooldown > 0) interactionCooldown--;
        if (lastTapFeedbackTick > 0) lastTapFeedbackTick--;
        
        if (questActive && !completed)
        {
            animTick++;
            checkInput();
            updateParticles();
            
            if (successfulDoubleTaps >= targetDoubleTaps)
            {
                finishQuest(true);
            }
            
            updateDisplay();
        }
        else if (completed)
        {
            resultScreenTick++;
            updateDisplay();
            if (resultScreenTick > 120)
            {
                GameState.getInstance().setMiniQuestActive(false);
            }
        }
    }
    
    private void checkInput()
    {
        if (Greenfoot.isKeyDown("space"))
        {
            int timeSinceLastPress = animTick - lastSpacePress;
            
            if (timeSinceLastPress > doubleTapWindow)
            {
                lastSpacePress = animTick;
                totalTaps++;
            }
            else if (timeSinceLastPress > 3 && timeSinceLastPress <= doubleTapWindow)
            {
                lastSpacePress = animTick;
                successfulDoubleTaps++;
                score += 2;
                lastTapFeedbackTick = 15;
                lastWasSuccess = true;
                emitParticles();
            }
        }
    }
    
    private void emitParticles()
    {
        World world = getWorld();
        if (world == null) return;
        int w = world.getWidth();
        int h = world.getHeight();
        int centerX = w / 2;
        int centerY = h / 2 + 100;
        
        for (int i = 0; i < 12; i++)
        {
            double angle = (i / 12.0) * Math.PI * 2;
            int vx = (int)(Math.cos(angle) * 8);
            int vy = (int)(Math.sin(angle) * 8);
            Color particleColor = new Color(255, 200, 100, 200);
            particles.add(new Particle(centerX, centerY, vx, vy, particleColor));
        }
    }
    
    private void updateParticles()
    {
        for (int i = particles.size() - 1; i >= 0; i--)
        {
            Particle p = particles.get(i);
            p.update();
            if (!p.alive())
            {
                particles.remove(i);
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
        
        int pulse = 70 + (int)(50 * Math.sin(animTick * 0.1));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);
        
        if (completed)
        {
            drawResultScreen(img, panelW, panelH);
        }
        else
        {
            drawQuestScreen(img, panelW, panelH);
        }
        
        setImage(img);
        overlay.setImage(img);
    }
    
    private void drawQuestScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowAlpha = 60 + (int)(40 * Math.sin(animTick * 0.08));
        img.setColor(new Color(255, 130, 100, glowAlpha));
        img.fillRect(px - 12, py - 12, panelW + 24, panelH + 24);
        
        // Panel background
        img.setColor(new Color(12, 12, 28, 250));
        img.fillRect(px, py, panelW, panelH);
        
        // Border with multiple layers for glow
        img.setColor(new Color(255, 140, 100, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(255, 150, 120, 140));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);
        img.setColor(new Color(255, 160, 130, 70));
        img.drawRect(px + 2, py + 2, panelW - 4, panelH - 4);
        
        // Title
        img.setColor(new Color(255, 180, 80));
        img.setFont(new greenfoot.Font("Arial", true, false, 34));
        img.drawString("DOUBLE TAP SPRINT", px + 50, py + 50);
        
        // Status display
        img.setFont(new greenfoot.Font("Arial", true, false, 22));
        img.setColor(new Color(120, 255, 120));
        img.drawString("Successful Taps: " + successfulDoubleTaps + "/" + targetDoubleTaps, px + 40, py + 110);
        
        img.setColor(new Color(255, 200, 120));
        img.drawString("Score: " + score, px + 40, py + 145);
        
        img.setColor(new Color(150, 180, 255));
        img.drawString("Total Presses: " + totalTaps, px + 40, py + 180);
        
        // Progress bar with glow
        int barW = 420;
        int barH = 35;
        int barX = px + 50;
        int barY = py + 230;
        
        img.setColor(new Color(50, 50, 70, 200));
        img.fillRect(barX - 4, barY - 4, barW + 8, barH + 8);
        img.setColor(new Color(255, 140, 100, 180));
        img.drawRect(barX - 4, barY - 4, barW + 8, barH + 8);
        img.setColor(new Color(40, 40, 60));
        img.fillRect(barX, barY, barW, barH);
        
        int progress = (int)((successfulDoubleTaps * 1.0 / targetDoubleTaps) * barW);
        for (int i = 0; i < progress; i++)
        {
            int shade = 150 + (i * 105) / barW;
            img.setColor(new Color(255, shade, 100, 230));
            img.drawLine(barX + i, barY, barX + i, barY + barH);
        }
        
        // Tap feedback flash
        if (lastTapFeedbackTick > 0)
        {
            int flashAlpha = (lastTapFeedbackTick * 255) / 15;
            if (lastWasSuccess)
            {
                img.setColor(new Color(255, 255, 255, flashAlpha / 3));
                img.fillRect(px + 10, py + 10, panelW - 20, panelH - 20);
            }
        }
        
        // Double-tap window indicator
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.setColor(new Color(180, 180, 200, 200));
        img.drawString("Press SPACE twice quickly", px + 120, py + 305);
        
        // Draw particles
        for (Particle p : particles)
        {
            int alpha = (p.life * 200) / p.maxLife;
            img.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha));
            img.fillOval(p.x - 3, p.y - 3, 6, 6);
        }
    }
    
    private void drawResultScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowColor = success ? 120 : 200;
        int glowColorG = success ? 255 : 120;
        img.setColor(new Color(glowColor, glowColorG, 100, 75));
        img.fillRect(px - 10, py - 10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(18, 18, 35, 250));
        img.fillRect(px, py, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, 100, 210));
        img.drawRect(px, py, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 36));
        String resultText = success ? "SUCCESS!" : "COMPLETE!";
        img.drawString(resultText, px + 70, py + 60);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("Double Taps: " + successfulDoubleTaps, px + 50, py + 120);
        img.drawString("Total Score: " + score, px + 50, py + 160);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.drawString("Sprint Challenge Complete!", px + 60, py + 240);
    }
    
    private void finishQuest(boolean success)
    {
        this.success = success;
        completed = true;
        questActive = false;
        resultScreenTick = 0;
        
        World world = getWorld();
        if (world == null) return;
        
        // Get overlay layer
        java.util.List<OverlayLayer> overlays = world.getObjects(OverlayLayer.class);
        if (overlays.isEmpty()) return;
        OverlayLayer overlay = overlays.get(0);
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        // Glow effect
        int glowColor = success ? 120 : 200;
        int glowColorG = success ? 255 : 120;
        img.setColor(new Color(glowColor, glowColorG, 100, 75));
        img.fillRect(-10, -10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(18, 18, 35, 250));
        img.fillRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, 100, 210));
        img.drawRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 32));
        String resultText = success ? "SUCCESS!" : "COMPLETE!";
        img.drawString(resultText, panelW / 2 - 70, panelH / 2 - 30);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Double Taps: " + successfulDoubleTaps, panelW / 2 - 90, panelH / 2 + 20);
        img.drawString("Total Score: " + score, panelW / 2 - 90, panelH / 2 + 55);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.drawString("Sprint Challenge Complete!", panelW / 2 - 105, panelH / 2 + 95);
        
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
    
    public java.awt.Rectangle[] getCollisionRects()
    {
        return new java.awt.Rectangle[] { new java.awt.Rectangle(getX() - 24, getY() - 24, 48, 48) };
    }
}
