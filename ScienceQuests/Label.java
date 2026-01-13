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
}
