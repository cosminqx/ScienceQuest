import greenfoot.*;

/**
 * EndingSequence - Shows fade-to-black animation with "Coming Soon" message when game is completed
 */
public class EndingSequence extends OverlayLayer
{
    private int animationTick = 0;
    private int maxTick = 120; // ~2 seconds at 60fps
    private boolean fadeComplete = false;
    private int displayTick = 0;
    
    public EndingSequence()
    {
        super();
    }
    
    @Override
    public void act()
    {
        super.act();
        
        animationTick++;
        
        // Draw fade-to-black with pulsing effect
        GreenfootImage img = new GreenfootImage(getWorld().getWidth(), getWorld().getHeight());
        img.setColor(new Color(0, 0, 0, 0));
        img.fillRect(0, 0, img.getWidth(), img.getHeight());
        
        // Calculate fade alpha
        int blackAlpha;
        if (animationTick < maxTick / 2)
        {
            // Fade in to black
            blackAlpha = (int)(255 * (animationTick / (double)(maxTick / 2)));
        }
        else if (animationTick < maxTick)
        {
            // Hold black
            blackAlpha = 255;
        }
        else
        {
            // Display text
            blackAlpha = 255;
            displayTick++;
        }
        
        // Draw semi-transparent black background
        img.setColor(new Color(0, 0, 0, blackAlpha));
        img.fillRect(0, 0, img.getWidth(), img.getHeight());
        
        // Show text after fade completes
        if (displayTick > 0 && displayTick <= 300)
        {
            // Pulsing text
            int textAlpha = (int)(128 + 127 * Math.sin(displayTick * 0.05));
            
            img.setColor(new Color(255, 255, 255, textAlpha));
            
            // Main title - "În curând..."
            img.setFont(FontManager.getPixeledLarge());
            String text = "In curand...";
            int textWidth = text.length() * 12; // Approximate width for pixeled font
            int textX = (img.getWidth() - textWidth) / 2;
            img.drawString(text, textX, img.getHeight() / 2 - 80);
            
            // Subtitle - "ScienceQuests 2"
            img.setFont(FontManager.getPixeled());
            String text2 = "ScienceQuests 2";
            int text2Width = text2.length() * 10;
            int text2X = (img.getWidth() - text2Width) / 2;
            img.drawString(text2, text2X, img.getHeight() / 2 - 40);
            
            // Credits section
            img.setFont(FontManager.getPixeled());
            String creditsTitle = "CREDITS";
            int creditsTitleWidth = creditsTitle.length() * 10;
            int creditsTitleX = (img.getWidth() - creditsTitleWidth) / 2;
            img.drawString(creditsTitle, creditsTitleX, img.getHeight() / 2 + 30);
            
            img.setFont(FontManager.getPixeledSmall());
            String credit1 = "Silviu Chiscareanu";
            int credit1Width = credit1.length() * 8;
            int credit1X = (img.getWidth() - credit1Width) / 2;
            img.drawString(credit1, credit1X, img.getHeight() / 2 + 60);
            
            String credit2 = "Cosmin Lacatus";
            int credit2Width = credit2.length() * 8;
            int credit2X = (img.getWidth() - credit2Width) / 2;
            img.drawString(credit2, credit2X, img.getHeight() / 2 + 85);
        }
        
        setImage(img);
    }
    
    public boolean isComplete()
    {
        return displayTick > 300;
    }
}
