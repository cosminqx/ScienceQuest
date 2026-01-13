import greenfoot.*;

public class Label extends Actor
{
    public Label(String text, int fontSize)
    {
        GreenfootImage image = new GreenfootImage(
            text,
            fontSize,
            Color.WHITE,
            new Color(0, 0, 0, 0) // transparent background
        );
        setImage(image);
    }
}
