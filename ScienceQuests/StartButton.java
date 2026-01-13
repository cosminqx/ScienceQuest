import greenfoot.*;

public class StartButton extends Actor
{
    public StartButton()
    {
        GreenfootImage image = new GreenfootImage(200, 50);
        image.setColor(Color.DARK_GRAY);
        image.fillRect(0, 0, 200, 50);

        image.setColor(Color.WHITE);
        image.drawString("START", 75, 32);

        setImage(image);
    }

    public void act()
    {
        if (Greenfoot.mouseClicked(this))
        {
            startGame();
        }
    }

    private void startGame()
    {
        Greenfoot.setWorld(new MainMapWorld());
    }
}
