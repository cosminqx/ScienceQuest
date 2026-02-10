import greenfoot.*;

/**
 * AlternatingKeysQuest - Rapidly alternate LEFT/RIGHT with glowing UI, pulse animations, and combo tracking
 */
public class AlternatingKeysQuest extends BaseQuest
{
    private int score = 0;
    private int combo = 0;
    private int maxCombo = 0;
    private int targetScore = 20;
    private boolean success = false;
    private int animTick = 0;
    private int correctFeedbackTick = 0;
    private String lastCorrectKey = "";
    private String expectedKey = "left";
    private int timeSinceLastPress = 0;
    private int feedbackDuration = 15;
    private int resultScreenTick = 0;
    private boolean leftDown = false;
    private boolean rightDown = false;
    
    public AlternatingKeysQuest(int mapX, int mapY)
    {
        super(mapX, mapY);
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
            updateResultOverlayTicks();
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
            boolean startPressed = Greenfoot.isKeyDown("space");
            if (canStartQuest(player, 100))
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
                        beginQuest();
                        score = 0;
                        combo = 0;
                        maxCombo = 0;
                        expectedKey = "left";
                        timeSinceLastPress = 0;
                        animTick = 0;
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
        if (correctFeedbackTick > 0) correctFeedbackTick--;
        
        if (questActive && !completed)
        {
            animTick++;
            timeSinceLastPress++;
            checkInput();
            
            if (timeSinceLastPress > 45)
            {
                combo = 0;
            }
            
            if (score >= targetScore)
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
                endQuest();
            }
        }
    }

    private void checkInput()
    {
        boolean leftPressed = Greenfoot.isKeyDown("left");
        boolean rightPressed = Greenfoot.isKeyDown("right");
        boolean leftJustPressed = leftPressed && !leftDown;
        boolean rightJustPressed = rightPressed && !rightDown;
        boolean anyJustPressed = leftJustPressed || rightJustPressed;
        
        if (anyJustPressed && timeSinceLastPress > 8)
        {
            String pressedKey = leftJustPressed ? "left" : "right";
            
            if (pressedKey.equals(expectedKey))
            {
                score++;
                combo++;
                if (combo > maxCombo) maxCombo = combo;
                correctFeedbackTick = feedbackDuration;
                lastCorrectKey = pressedKey;
                expectedKey = pressedKey.equals("left") ? "right" : "left";
                timeSinceLastPress = 0;
            }
            else
            {
                combo = 0;
                timeSinceLastPress = 0;
            }
        }
        
        leftDown = leftPressed;
        rightDown = rightPressed;
    }
    
    private void updateDisplay()
    {
        World world = getWorld();
        if (world == null) return;
        
        ensureOverlay();
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        int pulse = 80 + (int)(50 * Math.sin(animTick * 0.08));
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
        if (overlay != null)
        {
            overlay.setImage(img);
        }
    }
    
    private void drawQuestScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowAlpha = 50 + (int)(30 * Math.sin(animTick * 0.1));
        img.setColor(new Color(255, 120, 100, glowAlpha));
        img.fillRect(px - 10, py - 10, panelW + 20, panelH + 20);
        
        // Panel background
        img.setColor(new Color(15, 15, 30, 250));
        img.fillRect(px, py, panelW, panelH);
        
        // Border with glow
        img.setColor(new Color(255, 120, 100, 200));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(255, 140, 120, 100));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);
        img.drawRect(px + 2, py + 2, panelW - 4, panelH - 4);
        
        // Title
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 30));
        img.drawString("ALTERNARE SĂGEȚI", px + 65, py + 45);
        
        // Score and combo display
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(new Color(100, 255, 150));
        img.drawString("Scor: " + score + "/" + targetScore, px + 30, py + 100);
        
        img.setColor(new Color(255, 200, 100));
        img.drawString("Combo: " + combo, px + 30, py + 130);
        
        img.setColor(new Color(150, 150, 255));
        img.drawString("Max: " + maxCombo, px + 330, py + 130);
        
        // Progress bar with gradient
        int barW = 400;
        int barH = 30;
        int barX = px + 50;
        int barY = py + 170;
        
        img.setColor(new Color(40, 40, 60));
        img.fillRect(barX - 3, barY - 3, barW + 6, barH + 6);
        img.setColor(new Color(255, 120, 100, 150));
        img.drawRect(barX - 3, barY - 3, barW + 6, barH + 6);
        
        int progress = (int)((score * 1.0 / targetScore) * barW);
        for (int i = 0; i < progress; i++)
        {
            int shade = 100 + (i * 155) / barW;
            img.setColor(new Color(255, shade, 80, 220));
            img.drawLine(barX + i, barY, barX + i, barY + barH);
        }
        
        // Direction indicators with pulse
        int indicatorY = py + 240;
        int leftX = px + 80;
        int rightX = px + 380;
        
        // Left indicator
        int leftAlpha = 100;
        int leftPulse = 0;
        if (expectedKey.equals("left"))
        {
            leftPulse = (int)(20 * Math.sin(animTick * 0.15));
            leftAlpha = Math.min(255, 200 + leftPulse);
        }
        if (correctFeedbackTick > 0 && lastCorrectKey.equals("left"))
        {
            leftAlpha = 255;
        }
        
        img.setColor(new Color(100, 255, 150, leftAlpha));
        img.setFont(new greenfoot.Font("Arial", true, false, 40));
        img.drawString("←", leftX - 20, indicatorY);
        if (expectedKey.equals("left"))
        {
            img.setColor(new Color(100, 255, 150, 100 + leftPulse));
            img.fillOval(leftX - 35, indicatorY - 35, 30, 30);
        }
        
        // Right indicator
        int rightAlpha = 100;
        int rightPulse = 0;
        if (expectedKey.equals("right"))
        {
            rightPulse = (int)(20 * Math.sin(animTick * 0.15));
            rightAlpha = Math.min(255, 200 + rightPulse);
        }
        if (correctFeedbackTick > 0 && lastCorrectKey.equals("right"))
        {
            rightAlpha = 255;
        }
        
        img.setColor(new Color(150, 200, 255, rightAlpha));
        img.drawString("→", rightX, indicatorY);
        if (expectedKey.equals("right"))
        {
            img.setColor(new Color(150, 200, 255, 100 + rightPulse));
            img.fillOval(rightX + 10, indicatorY - 35, 30, 30);
        }
        
        // Instructions
        img.setColor(new Color(200, 200, 200, 180));
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.drawString("INSTRUCȚIUNI: apasă stânga/dreapta alternativ", px + 60, py + 300);
    }
    
    private void drawResultScreen(GreenfootImage img, int w, int h)
    {
        int panelW = w;
        int panelH = h;
        int px = 0;
        int py = 0;
        
        // Glow effect
        int glowColor = success ? 100 : 200;
        int glowColorG = success ? 255 : 100;
        int glowColorB = success ? 100 : 100;
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 70));
        img.fillRect(px - 10, py - 10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(20, 20, 40, 250));
        img.fillRect(px, py, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 200));
        img.drawRect(px, py, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 32));
        String resultText = success ? "SUCCES!" : "COMPLET!";
        img.drawString(resultText, px + 80, py + 60);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("Scor final: " + score, px + 50, py + 120);
        img.drawString("Combo maxim: " + maxCombo, px + 50, py + 160);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 16));
        img.drawString("Misiune completă!", px + 90, py + 240);
    }

    private void showTutorial()
    {
        World world = getWorld();
        if (world == null) return;
        ensureOverlay();

        int w = 440;
        int h = 180;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 200));
        img.fillRect(0, 0, w, h);
        img.setColor(new Color(255, 140, 120, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: ALTERNARE", 120, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Apasă STÂNGA și DREAPTA alternativ.", 80, 70);
        img.drawString("Scop: atinge " + targetScore + " puncte.", 115, 95);
        img.setColor(new Color(200, 255, 200));
        img.drawString("Apasă SPATIU pentru a începe", 130, 140);

        if (overlay != null)
        {
            overlay.setImage(img);
        }
    }
    
    private void finishQuest(boolean success)
    {
        this.success = success;
        completed = true;
        questActive = false;
        resultScreenTick = 0;
        resultDisplayTicks = 120;
        endQuest();
        
        World world = getWorld();
        if (world == null || overlay == null) return;
        
        int panelW = 460;
        int panelH = 280;
        GreenfootImage img = new GreenfootImage(panelW, panelH);
        
        // Glow effect
        int glowColor = success ? 100 : 200;
        int glowColorG = success ? 255 : 100;
        int glowColorB = success ? 100 : 100;
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 70));
        img.fillRect(-10, -10, panelW + 20, panelH + 20);
        
        img.setColor(new Color(20, 20, 40, 250));
        img.fillRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(glowColor, glowColorG, glowColorB, 200));
        img.drawRect(0, 0, panelW, panelH);
        
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 32));
        String resultText = success ? "SUCCES!" : "COMPLET!";
        img.drawString(resultText, panelW / 2 - 70, panelH / 2 - 30);
        
        img.setColor(new Color(255, 200, 100));
        img.setFont(new greenfoot.Font("Arial", true, false, 22));
        img.drawString("Scor final: " + score, panelW / 2 - 90, panelH / 2 + 30);
        img.drawString("Combo maxim: " + maxCombo, panelW / 2 - 90, panelH / 2 + 65);
        
        img.setColor(new Color(150, 200, 255));
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.drawString("Misiune completă!", panelW / 2 - 75, panelH / 2 + 105);
        
        // Set transparent actor image
        GreenfootImage transparent = new GreenfootImage(48, 48);
        transparent.setColor(new Color(0, 0, 0, 0));
        transparent.fillRect(0, 0, 48, 48);
        setImage(transparent);
        
        overlay.setImage(img);
    }
    
    public int getMapX() { return mapX; }
    public int getMapY() { return mapY; }
    
    public java.awt.Rectangle[] getCollisionRects()
    {
        return new java.awt.Rectangle[] { new java.awt.Rectangle(getX() - 24, getY() - 24, 48, 48) };
    }
}
