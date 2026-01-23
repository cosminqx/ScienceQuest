import greenfoot.*;

/**
 * InputField - Text input field for player name entry
 */
public class InputField extends Actor
{
    private String text = "";
    private int maxLength;
    private boolean isFocused;
    private boolean isHovered;
    private int width;
    private int height;
    private int caretTimer = 0;
    private boolean showCaret = true;

    public InputField(int width, int height, int maxLength)
    {
        this.width = width;
        this.height = height;
        this.maxLength = maxLength;
        this.isFocused = true;
        this.isHovered = false;
        updateImage();
    }

    public void act()
    {
        // Check if mouse is hovering
        MouseInfo mouse = Greenfoot.getMouseInfo();
        if (mouse != null)
        {
            int mouseX = mouse.getX();
            int mouseY = mouse.getY();
            int myX = getX();
            int myY = getY();
            
            boolean nowHovered = Math.abs(mouseX - myX) < width / 2 && Math.abs(mouseY - myY) < height / 2;
            
            if (nowHovered != isHovered)
            {
                isHovered = nowHovered;
                updateImage();
            }
        }
        
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
        
        // Animate caret blinking
        if (isFocused)
        {
            caretTimer++;
            if (caretTimer >= 30) // Blink every 30 frames
            {
                caretTimer = 0;
                showCaret = !showCaret;
                updateImage();
            }
        }
    }

    private void updateImage()
    {
        GreenfootImage image = new GreenfootImage(width, height);

        // Solid color - no hover or focus states
        image.setColor(new Color(255, 52, 112));
        image.fillRect(0, 0, width, height);
        image.setColor(new Color(255, 82, 132));
        image.fillRect(2, 2, width - 4, height - 4);

        // Border
        image.setColor(new Color(50, 70, 110));
        image.drawRect(0, 0, width - 1, height - 1);

        // Draw text
        image.setColor(Color.WHITE);
        image.drawString(text, 10, height / 2 + 5);
        
        // Draw caret
        if (isFocused && showCaret)
        {
            int textWidth = new GreenfootImage(text, 16, Color.WHITE, new Color(0, 0, 0, 0)).getWidth();
            int caretX = 10 + textWidth + 2;
            image.setColor(Color.WHITE);
            image.drawLine(caretX, height / 2 - 8, caretX, height / 2 + 8);
        }

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
