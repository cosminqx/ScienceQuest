import greenfoot.*;

/**
 * InputField - Text input field for player name entry
 */
public class InputField extends Actor
{
    private String text = "";
    private int maxLength;
    private boolean isFocused;
    private int width;
    private int height;

    public InputField(int width, int height, int maxLength)
    {
        this.width = width;
        this.height = height;
        this.maxLength = maxLength;
        this.isFocused = true;
        updateImage();
    }

    public void act()
    {
        // Handle keyboard input
        String key = Greenfoot.getKey();
        if (key != null)
        {
            if (key.equals("backspace") && text.length() > 0)
            {
                text = text.substring(0, text.length() - 1);
            }
            else if (key.length() == 1 && text.length() < maxLength)
            {
                text += key;
            }
            updateImage();
        }
    }

    private void updateImage()
    {
        GreenfootImage image = new GreenfootImage(width, height);
        image.setColor(Color.WHITE);
        image.drawRect(0, 0, width - 1, height - 1);

        if (isFocused)
        {
            image.setColor(new Color(100, 150, 255));
            image.fillRect(1, 1, width - 2, height - 2);
        }
        else
        {
            image.setColor(new Color(200, 200, 200));
            image.fillRect(1, 1, width - 2, height - 2);
        }

        image.setColor(Color.BLACK);
        image.drawString(text, 10, height / 2 + 5);

        setImage(image);
    }

    public String getText()
    {
        return text;
    }

    public void setText(String newText)
    {
        text = newText;
        updateImage();
    }

    public void setFocused(boolean focused)
    {
        isFocused = focused;
        updateImage();
    }
}
