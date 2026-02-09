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
        // Smaller and more compact
        int w = 140;
        int h = 90;
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

        // Smaller background rectangle
        img.setColor(new Color(20, 20, 40, 160));
        img.fillRect(6, 6, w - 12, h - 12);

        // Smaller arrow
        img.setFont(new greenfoot.Font("Arial", true, false, 42));
        img.setColor(new Color(255, 230, 120, 255));
        img.drawString(arrow, w / 2 - 12, 45);

        // Smaller text with Pixeled font
        img.setFont(FontManager.getPixeledSmall());
        img.setColor(new Color(255, 255, 255, 255));
        int labelX = Math.max(8, (w - (label.length() * 5)) / 2);
        img.drawString(label, labelX, 70);

        return img;
    }
}
