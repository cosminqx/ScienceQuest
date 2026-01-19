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

        // Render text once to measure exact width/height
        GreenfootImage textImg = new GreenfootImage(text, size, textColor, new Color(0, 0, 0, 0));

        // Canvas wide enough for StartWorld (600px) plus padding
        int padding = size; // simple padding around text
        int width = Math.max(textImg.getWidth() + padding * 2, 600);
        int height = Math.max(textImg.getHeight() + padding * 2, size * 2);

        GreenfootImage img = new GreenfootImage(width, height);
        img.setColor(new Color(0, 0, 0, 0));
        img.clear();
        img.setFont(font);
        img.setColor(textColor);

        // Center text image precisely
        int textX = (width - textImg.getWidth()) / 2;
        int textY = (height - textImg.getHeight()) / 2;
        img.drawImage(textImg, textX, textY);
        setImage(img);
    }
}
