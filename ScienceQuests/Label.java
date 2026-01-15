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
     * Custom font variant: draws text with a supplied font (e.g., 8-BIT WONDER).
     */
    public Label(String text, Font font, Color textColor)
    {
        int size = font.getSize();
        int width = Math.max(10, text.length() * size + 20);
        int height = size * 2;

        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(new Color(0, 0, 0, 0));
        img.clear();
        img.setFont(font);
        img.setColor(textColor);
        int baseline = (int)(size * 1.1);
        img.drawString(text, 10, baseline);
        setImage(img);
    }
}
