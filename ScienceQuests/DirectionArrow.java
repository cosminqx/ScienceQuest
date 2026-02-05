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
        // More subtle - smaller and less opaque
        int w = 160;
        int h = 100;
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

        // More subtle background - darker and less visible
        img.setColor(new Color(0, 0, 0, 80));
        img.fillRect(8, 8, w - 16, h - 16);

        // Smaller, more muted arrow
        img.setFont(new greenfoot.Font("Arial", true, false, 42));
        img.setColor(new Color(180, 160, 100, 200));
        img.drawString(arrow, w / 2 - 12, 50);

        // Smaller, more subtle text
        img.setFont(new greenfoot.Font("Arial", false, false, 12));
        img.setColor(new Color(200, 200, 200, 180));
        int labelX = Math.max(10, (w - (label.length() * 5)) / 2);
        img.drawString(label, labelX, 75);

        return img;
    }
}
