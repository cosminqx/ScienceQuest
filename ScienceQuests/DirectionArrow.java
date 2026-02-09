import greenfoot.*;

/**
 * DirectionArrow - Visual hint arrow with a label for map exits.
 */
public class DirectionArrow extends Actor
{
    public DirectionArrow(String direction, String label)
    {
        setImage(buildImage(direction, label));
    }

    private GreenfootImage buildImage(String direction, String label)
    {
        // Make it more visible - larger and more opaque
        int w = 220;
        int h = 140;
        GreenfootImage img = new GreenfootImage(w, h);
        img.setColor(new Color(0, 0, 0, 0));
        img.fillRect(0, 0, w, h);

        String arrow = "↓";
        if ("left".equalsIgnoreCase(direction))
        {
            arrow = "←";
        }
        else if ("right".equalsIgnoreCase(direction))
        {
            arrow = "→";
        }
        else if ("up".equalsIgnoreCase(direction))
        {
            arrow = "↑";
        }

        // More visible background - brighter and more opaque
        img.setColor(new Color(20, 20, 40, 160));
        img.fillRect(8, 8, w - 16, h - 16);

        // Larger, brighter arrow with glow effect
        img.setFont(new greenfoot.Font("Arial", true, false, 64));
        img.setColor(new Color(255, 230, 120, 255));
        img.drawString(arrow, w / 2 - 18, 68);

        // Larger, more visible text with Pixeled font
        img.setFont(FontManager.getPixeledLarge());
        img.setColor(new Color(255, 255, 255, 255));
        int labelX = Math.max(10, (w - (label.length() * 7)) / 2);
        img.drawString(label, labelX, 105);

        return img;
    }
}
