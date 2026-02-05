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
    private OverlayLayer myOverlay = null;
    private int resultDisplayTicks = 0;
    private int baseY = 0;
    private boolean baseYSet = false;
    private int floatTick = 0;
    private boolean startKeyDown = false;
    private boolean spaceDown = false;
    
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
                score = 0;
                totalTaps = 0;
                successfulDoubleTaps = 0;
                lastSpacePress = -100;
                animTick = 0;
                particles.clear();
                spaceDown = false;
                GameState.getInstance().setMiniQuestActive(true);
                interactionCooldown = 10;
            }
            startKeyDown = startPressed;
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
        boolean spacePressed = Greenfoot.isKeyDown("space");
        if (spacePressed && !spaceDown)
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
        spaceDown = spacePressed;
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
        if (myOverlay == null || myOverlay.getWorld() == null)
        {
            myOverlay = new OverlayLayer();
            world.addObject(myOverlay, world.getWidth() / 2, world.getHeight() / 2);
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
        myOverlay.setImage(img);
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
        img.setFont(new greenfoot.Font("Arial", true, false, 28));
        img.drawString("SPRINT CU DUBLĂ APĂSARE", px + 20, py + 50);
        
        // Status display
        img.setFont(new greenfoot.Font("Arial", true, false, 22));
        img.setColor(new Color(120, 255, 120));
        img.drawString("Duble reușite: " + successfulDoubleTaps + "/" + targetDoubleTaps, px + 40, py + 110);
        
        img.setColor(new Color(255, 200, 120));
        img.drawString("Scor: " + score, px + 40, py + 145);
        
        img.setColor(new Color(150, 180, 255));
        img.drawString("Apăsări totale: " + totalTaps, px + 40, py + 180);
        
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
        img.drawString("INSTRUCȚIUNI: apasă SPATIU de două ori rapid", px + 35, py + 305);
        
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
        String resultText = success ? "SUCCES!" : "COMPLET!";
        img.drawString(resultText, px + 70, py + 60);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("Duble: " + successfulDoubleTaps, px + 50, py + 120);
        img.drawString("Scor total: " + score, px + 50, py + 160);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.drawString("Provocare de sprint completă!", px + 20, py + 240);
    }
    
    private void finishQuest(boolean success)
    {
        this.success = success;
        completed = true;
        questActive = false;
        resultScreenTick = 0;
        resultDisplayTicks = 120;
        
        World world = getWorld();
        if (world == null || myOverlay == null) return;
        
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
        String resultText = success ? "SUCCES!" : "COMPLET!";
        img.drawString(resultText, panelW / 2 - 70, panelH / 2 - 30);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Duble: " + successfulDoubleTaps, panelW / 2 - 80, panelH / 2 + 20);
        img.drawString("Scor total: " + score, panelW / 2 - 80, panelH / 2 + 55);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.drawString("Provocare de sprint completă!", panelW / 2 - 130, panelH / 2 + 95);
        
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
    
    public java.awt.Rectangle[] getCollisionRects()
    {
        return new java.awt.Rectangle[] { new java.awt.Rectangle(getX() - 24, getY() - 24, 48, 48) };
    }
}
