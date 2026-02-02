import greenfoot.*;

/**
 * TimingQuestUI - Visual interface for the timing mini-quest
 * Player must press space when the moving indicator is in the green success zone
 */
public class TimingQuestUI extends Actor
{
    private int barWidth = 400;
    private int barHeight = 60;
    private int successZoneStart = 160; // Position where success zone starts
    private int successZoneWidth = 80;
    private int indicatorPos = 0;
    private int indicatorSpeed = 4;
    private boolean movingRight = true;
    private boolean completed = false;
    private boolean failed = false;
    private int failCooldown = 0;
    private QuestBlock parentQuest;
    
    public TimingQuestUI(QuestBlock parent)
    {
        this.parentQuest = parent;
        updateImage();
    }
    
    public void act()
    {
        if (completed || (failed && failCooldown > 0))
        {
            if (failed)
            {
                failCooldown--;
                if (failCooldown <= 0)
                {
                    failed = false;
                }
            }
            updateImage();
            return;
        }
        
        // Move the indicator back and forth
        if (movingRight)
        {
            indicatorPos += indicatorSpeed;
            if (indicatorPos >= barWidth - 10)
            {
                indicatorPos = barWidth - 10;
                movingRight = false;
            }
        }
        else
        {
            indicatorPos -= indicatorSpeed;
            if (indicatorPos <= 0)
            {
                indicatorPos = 0;
                movingRight = true;
            }
        }
        
        // Check if player presses space
        if (Greenfoot.isKeyDown("space"))
        {
            checkTiming();
        }
        
        updateImage();
    }
    
    private void checkTiming()
    {
        // Check if indicator is in the success zone
        if (indicatorPos >= successZoneStart && 
            indicatorPos <= successZoneStart + successZoneWidth)
        {
            completed = true;
            DebugLog.log("SUCCESS! Perfect timing!");
        }
        else
        {
            failed = true;
            failCooldown = 30; // 0.5 second cooldown before trying again
            DebugLog.log("FAILED! Try again!");
        }
    }
    
    private void updateImage()
    {
        GreenfootImage image = new GreenfootImage(barWidth + 40, barHeight + 100);
        
        // Semi-transparent background
        image.setColor(new Color(0, 0, 0, 180));
        image.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        // Title text
        image.setColor(Color.WHITE);
        image.setFont(new greenfoot.Font("Arial", true, false, 20));
        image.drawString("Press SPACE at the right time!", 20, 30);
        
        // Bar background
        image.setColor(new Color(60, 60, 60));
        image.fillRect(20, 50, barWidth, barHeight);
        
        // Success zone (green)
        if (!completed)
        {
            image.setColor(new Color(0, 200, 0, 150));
            image.fillRect(20 + successZoneStart, 50, successZoneWidth, barHeight);
        }
        
        // Moving indicator
        if (completed)
        {
            image.setColor(new Color(0, 255, 0)); // Bright green for success
        }
        else if (failed && failCooldown > 0)
        {
            image.setColor(new Color(255, 0, 0)); // Red for failure
        }
        else
        {
            image.setColor(new Color(255, 255, 0)); // Yellow for normal
        }
        image.fillRect(20 + indicatorPos, 45, 10, barHeight + 10);
        
        // Status text
        if (completed)
        {
            image.setColor(new Color(0, 255, 0));
            image.setFont(new greenfoot.Font("Arial", true, false, 18));
            image.drawString("SUCCESS! Block removed!", 80, 130);
        }
        else if (failed && failCooldown > 0)
        {
            image.setColor(new Color(255, 100, 100));
            image.setFont(new greenfoot.Font("Arial", true, false, 18));
            image.drawString("Try again!", 150, 130);
        }
        
        setImage(image);
    }
    
    public boolean isCompleted()
    {
        return completed;
    }
    
    public boolean isFailed()
    {
        return failed && failCooldown == 0;
    }
    
    public void reset()
    {
        failed = false;
        failCooldown = 0;
        indicatorPos = 0;
    }
}
