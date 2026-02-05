import greenfoot.*;

/**
 * KeySequenceQuest - Complete key sequences with visual feedback
 */
public class KeySequenceQuest extends Actor
{
    private int mapX, mapY;
    private int sequenceIndex = 0;
    private int targetSequences = 4;
    private int completedSequences = 0;
    private static final int SEQUENCE_LENGTH = 5;
    private String[] sequence = new String[SEQUENCE_LENGTH];
    private int timeRemaining = 600;
    private int timeMax = 600;
    private boolean questActive = false;
    private boolean completed = false;
    private int interactionCooldown = 0;
    private int resetCooldown = 0;
    private int animTick = 0;
    private int correctFeedbackTick = 0;
    private int wrongFeedbackTick = 0;
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
    
    public KeySequenceQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        generateSequence();
        createImage();
    }
    
    private void generateSequence()
    {
        String[] keys = {"up", "down", "left", "right"};
        String last = null;
        for (int i = 0; i < SEQUENCE_LENGTH; i++)
        {
            String next = keys[Greenfoot.getRandomNumber(4)];
            while (next.equals(last))
            {
                next = keys[Greenfoot.getRandomNumber(4)];
            }
            sequence[i] = next;
            last = next;
        }
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
                        sequenceIndex = 0;
                        completedSequences = 0;
                        animTick = 0;
                        timeRemaining = timeMax;
                        upDown = false;
                        downDown = false;
                        leftDown = false;
                        rightDown = false;
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
        if (resetCooldown > 0) resetCooldown--;
        if (correctFeedbackTick > 0) correctFeedbackTick--;
        if (wrongFeedbackTick > 0) wrongFeedbackTick--;
        
        if (questActive)
        {
            animTick++;
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
        String currentKey = sequence[sequenceIndex];
        boolean upPressed = Greenfoot.isKeyDown("up");
        boolean downPressed = Greenfoot.isKeyDown("down");
        boolean leftPressed = Greenfoot.isKeyDown("left");
        boolean rightPressed = Greenfoot.isKeyDown("right");
        boolean upJust = upPressed && !upDown;
        boolean downJust = downPressed && !downDown;
        boolean leftJust = leftPressed && !leftDown;
        boolean rightJust = rightPressed && !rightDown;
        boolean anyJust = upJust || downJust || leftJust || rightJust;
        
        String pressedKey = null;
        if (upJust) pressedKey = "up";
        else if (downJust) pressedKey = "down";
        else if (leftJust) pressedKey = "left";
        else if (rightJust) pressedKey = "right";
        
        if (anyJust && resetCooldown == 0)
        {
            if (pressedKey != null && pressedKey.equals(currentKey))
            {
                sequenceIndex++;
                correctFeedbackTick = 10;
                resetCooldown = 10;
                
                if (sequenceIndex >= sequence.length)
                {
                    completedSequences++;
                    if (completedSequences >= targetSequences)
                    {
                        finishQuest(true);
                    }
                    else
                    {
                        generateSequence();
                        sequenceIndex = 0;
                    }
                }
            }
            else
            {
                wrongFeedbackTick = 12;
                resetCooldown = 12;
                sequenceIndex = 0;
                generateSequence();
            }
        }
        
        upDown = upPressed;
        downDown = downPressed;
        leftDown = leftPressed;
        rightDown = rightPressed;
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
        int panelH = 300;
        GreenfootImage img = new GreenfootImage(panelW, panelH);

        int pulse = 100 + (int)(60 * Math.sin(animTick * 0.12));
        img.setColor(new Color(0, 0, 0, pulse));
        img.fillRect(0, 0, panelW, panelH);

        int px = 0;
        int py = 0;

        // Glow effect
        img.setColor(new Color(100, 150, 255, 60));
        img.fillRect(px - 8, py - 8, panelW + 16, panelH + 16);

        // Panel
        img.setColor(new Color(10, 10, 20, 245));
        img.fillRect(px, py, panelW, panelH);

        img.setColor(new Color(100, 150, 255, 220));
        img.drawRect(px, py, panelW, panelH);
        img.setColor(new Color(120, 170, 255, 120));
        img.drawRect(px + 1, py + 1, panelW - 2, panelH - 2);

        // Title
        img.setColor(new Color(255, 255, 255));
        img.setFont(new greenfoot.Font("Arial", true, false, 24));
        img.drawString("MAESTRUL SECVENȚEI", px + 70, py + 38);

        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(170, 200, 255));
        img.drawString("INSTRUCȚIUNI: apasă săgețile în ordinea afișată", px + 40, py + 58);

        // Sequence display
        int seqStartX = px + 60;
        int seqY = py + 80;
        int boxSize = 40;
        int gap = 12;
        for (int i = 0; i < SEQUENCE_LENGTH; i++)
        {
            int bx = seqStartX + i * (boxSize + gap);
            Color boxColor = new Color(50, 60, 80, 220);
            Color borderColor = new Color(120, 170, 255, 140);
            if (i < sequenceIndex)
            {
                boxColor = new Color(80, 180, 120, 220);
                borderColor = new Color(120, 255, 170, 200);
            }
            else if (i == sequenceIndex)
            {
                int glow = 140 + (int)(80 * Math.sin(animTick * 0.2));
                boxColor = new Color(70, 90, 130, 230);
                borderColor = new Color(150, 220, 255, glow);
            }
            else if (wrongFeedbackTick > 0)
            {
                borderColor = new Color(255, 100, 100, 160);
            }
            
            img.setColor(boxColor);
            img.fillRect(bx, seqY, boxSize, boxSize);
            img.setColor(borderColor);
            img.drawRect(bx, seqY, boxSize, boxSize);
            img.setColor(new Color(200, 200, 220));
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.drawString(getArrowForKey(sequence[i]), bx + 12, seqY + 28);
        }

        // Current key to press
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        String keyStr = sequence[sequenceIndex].toUpperCase();
        String arrow = getArrowForKey(sequence[sequenceIndex]);
        
        img.setColor(correctFeedbackTick > 0 ? new Color(100, 255, 100) :
                     wrongFeedbackTick > 0 ? new Color(255, 120, 120) : new Color(200, 200, 200));
        img.drawString("Apasă: " + keyStr + " " + arrow, px + 160, py + 150);

        // Sequence counter
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.setColor(new Color(255, 200, 100));
        img.drawString("Secvențe: " + completedSequences + " / " + targetSequences, px + 80, py + 155);
        img.drawString("Pas: " + (sequenceIndex + 1) + " / " + SEQUENCE_LENGTH, px + 285, py + 185);

        // Progress bar for current sequence
        float progress = (sequenceIndex + 1) / (float)SEQUENCE_LENGTH;
        int barW = (int)((panelW - 40) * progress);
        img.setColor(new Color(100, 200, 255, 150));
        img.fillRect(px + 20, py + 220, barW, 15);
        img.setColor(new Color(120, 220, 255, 200));
        img.drawRect(px + 20, py + 220, panelW - 40, 15);

        // Time bar
        float timePct = timeRemaining / (float)timeMax;
        int timeW = (int)((panelW - 40) * timePct);
        img.setColor(new Color(120, 120, 255, 80));
        img.fillRect(px + 20, py + 245, timeW, 8);
        img.setColor(new Color(150, 150, 255, 180));
        img.drawRect(px + 20, py + 245, panelW - 40, 8);
        img.setFont(new greenfoot.Font("Arial", true, false, 12));
        img.setColor(new Color(200, 200, 220));
        img.drawString(Math.round(timeRemaining / 60.0f) + "s", px + panelW - 45, py + 258);

        myOverlay.setImage(img);
    }
    
    private String getArrowForKey(String key)
    {
        if (key.equals("up")) return "↑";
        if (key.equals("down")) return "↓";
        if (key.equals("left")) return "←";
        if (key.equals("right")) return "→";
        return "";
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
        int h = 200;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 200));
        img.fillRect(0, 0, w, h);
        img.setColor(new Color(120, 170, 255, 200));
        img.drawRect(0, 0, w - 1, h - 1);

        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.setColor(Color.WHITE);
        img.drawString("TUTORIAL: SECVENȚE", 130, 30);
        img.setFont(new greenfoot.Font("Arial", false, false, 14));
        img.setColor(new Color(220, 220, 220));
        img.drawString("Apasă săgețile în ordinea afișată.", 90, 70);
        img.drawString("Completează " + targetSequences + " secvențe de " + SEQUENCE_LENGTH + " pași.", 70, 95);
        img.setColor(new Color(200, 255, 200));
        img.drawString("Apasă SPATIU pentru a începe", 140, 150);

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
        int score = completedSequences * 150;

        if (success)
        {
            img.setColor(new Color(0, 200, 50, 190));
            img.fillRect(0, 0, panelW, panelH);
            img.setColor(new Color(100, 255, 150, 100));
            img.fillRect(-8, -8, panelW + 16, panelH + 16);

            img.setColor(new Color(255, 255, 255));
            img.setFont(new greenfoot.Font("Arial", true, false, 38));
            img.drawString("SUCCES!", panelW / 2 - 100, panelH / 2 - 30);
            img.setFont(new greenfoot.Font("Arial", true, false, 20));
            img.drawString("Scor: " + score, panelW / 2 - 70, panelH / 2 + 50);
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
