import greenfoot.*;

public class Label extends Actor
{
    public Label(String text, int fontSize)
    {
        this(text, fontSize, Color.WHITE);
    }

    public Label(String text, int fontSize, Color textColor)
    {
        // Use GreenfootImage text constructor for proper rendering
        GreenfootImage image = new GreenfootImage(
            text,
            fontSize,
            textColor,
            new Color(0, 0, 0, 0) // transparent background
        );
        setImage(image);
    }

    /**
     * Custom font variant: draws text with a supplied Greenfoot font (e.g., Pixelated font from FontManager).
     */
    public Label(String text, greenfoot.Font font, Color textColor)
    {
        // Create a temporary image to measure text size
        GreenfootImage tempImg = new GreenfootImage(1, 1);
        tempImg.setFont(font);
        
        // Estimate text dimensions (Greenfoot doesn't have direct text measurement)
        // Use a scale factor based on font size - pixel fonts are roughly square
        int fontSize = font.getSize();
        int estimatedWidth = text.length() * fontSize + 20;
        int estimatedHeight = fontSize * 3;
        
        // Create the actual image with proper size
        GreenfootImage img = new GreenfootImage(estimatedWidth, estimatedHeight);
        img.setColor(new Color(0, 0, 0, 0));
        img.clear();
        img.setFont(font);
        img.setColor(textColor);
        
        // Draw text at appropriate position
        img.drawString(text, 10, fontSize + 5);
        
        setImage(img);
    }
}
