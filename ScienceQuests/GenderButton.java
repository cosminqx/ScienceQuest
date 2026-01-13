import greenfoot.*;

/**
 * GenderButton - Button for selecting player gender
 */
public class GenderButton extends Actor
{
    private String gender;
    private boolean isSelected;

    public GenderButton(String gender)
    {
        this.gender = gender;
        this.isSelected = false;
        updateImage();
    }

    public void act()
    {
        if (Greenfoot.mouseClicked(this))
        {
            isSelected = !isSelected;
            updateImage();
        }
    }

    private void updateImage()
    {
        GreenfootImage image = new GreenfootImage(120, 50);

        if (isSelected)
        {
            image.setColor(new Color(100, 150, 255));
        }
        else
        {
            image.setColor(Color.DARK_GRAY);
        }

        image.fillRect(0, 0, 120, 50);
        image.setColor(Color.WHITE);
        image.drawString(gender, 30, 32);

        setImage(image);
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public String getGender()
    {
        return gender;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
        updateImage();
    }
}
