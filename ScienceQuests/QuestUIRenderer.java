import greenfoot.*;

/**
 * QuestUIRenderer - Utility class for rendering stylish quest UI elements
 */
public class QuestUIRenderer
{
    public static void drawStyledPanel(GreenfootImage img, int px, int py, int w, int h, 
                                       Color glowColor, String title, int animTick)
    {
        // Outer glow
        img.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 70));
        img.fillRect(px - 8, py - 8, w + 16, h + 16);
        
        // Inner glow
        img.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 40));
        img.fillRect(px - 4, py - 4, w + 8, h + 8);
        
        // Main panel
        img.setColor(new Color(15, 15, 25, 240));
        img.fillRect(px, py, w, h);
        
        // Animated border
        Color borderColor = glowColor;
        img.setColor(borderColor);
        img.drawRect(px, py, w, h);
        img.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 120));
        img.drawRect(px + 1, py + 1, w - 2, h - 2);
    }
    
    public static void drawProgressBar(GreenfootImage img, int px, int py, int w, int h,
                                       float progress, Color barColor, int feedbackTick)
    {
        // Background
        img.setColor(new Color(40, 40, 50, 180));
        img.fillRect(px, py, w, h);
        
        // Bar glow
        int barW = (int)(w * progress);
        img.setColor(new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 100));
        img.fillRect(px - 2, py - 3, barW + 4, h + 6);
        
        // Main bar
        img.setColor(barColor);
        img.fillRect(px, py, barW, h);
        
        // Feedback flash
        if (feedbackTick > 0)
        {
            img.setColor(new Color(255, 255, 255, feedbackTick * 30));
            img.fillRect(px, py, barW, h);
        }
        
        // Border
        img.setColor(new Color(100, 100, 120, 150));
        img.drawRect(px, py, w, h);
    }
    
    public static Color getProgressColor(float progress)
    {
        if (progress < 0.5f) return new Color(255, 100, 100);    // Red
        if (progress < 0.75f) return new Color(255, 200, 50);    // Orange
        if (progress < 1.0f) return new Color(255, 255, 50);     // Yellow
        return new Color(50, 255, 100);                            // Green
    }
    
    public static void drawSuccessScreen(GreenfootImage img, int w, int h, 
                                        String title, String subtitle, int score)
    {
        img.setColor(new Color(0, 200, 50, 180));
        img.fillRect(0, 0, w, h);
        
        // Success glow
        img.setColor(new Color(100, 255, 150, 100));
        img.fillRect(w / 2 - 200, h / 2 - 100, 400, 200);
        
        img.setColor(Color.WHITE);
        img.setFont(new greenfoot.Font("Arial", true, false, 44));
        img.drawString(title, w / 2 - 60, h / 2 - 30);
        
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString(subtitle, w / 2 - 150, h / 2 + 50);
        
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Score: " + score, w / 2 - 60, h / 2 + 80);
    }
    
    public static void drawFailureScreen(GreenfootImage img, int w, int h,
                                        String title, String subtitle, int score)
    {
        img.setColor(new Color(200, 0, 50, 180));
        img.fillRect(0, 0, w, h);
        
        // Failure glow
        img.setColor(new Color(255, 100, 100, 100));
        img.fillRect(w / 2 - 200, h / 2 - 100, 400, 200);
        
        img.setColor(Color.WHITE);
        img.setFont(new greenfoot.Font("Arial", true, false, 44));
        img.drawString(title, w / 2 - 50, h / 2 - 30);
        
        img.setFont(new greenfoot.Font("Arial", true, false, 18));
        img.drawString(subtitle, w / 2 - 150, h / 2 + 50);
        
        img.setFont(new greenfoot.Font("Arial", true, false, 20));
        img.drawString("Score: " + score, w / 2 - 60, h / 2 + 80);
    }
}
