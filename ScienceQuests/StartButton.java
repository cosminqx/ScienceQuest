import greenfoot.*;

public class StartButton extends Actor
{
    private String label;

    public StartButton()
    {
        this("START");
    }

    public StartButton(String label)
    {
        this.label = label;
        updateImage();
    }

    private void updateImage()
    {
        GreenfootImage image = new GreenfootImage(200, 50);
        image.setColor(Color.DARK_GRAY);
        image.fillRect(0, 0, 200, 50);

        image.setColor(Color.WHITE);
        int stringWidth = label.length() * 6;
        image.drawString(label, 100 - stringWidth/2, 32);

        setImage(image);
    }

    public void act()
    {
        if (Greenfoot.mouseClicked(this))
        {
            onButtonClicked();
        }
    }

    public void onButtonClicked()
    {
        // Default behavior - override in subclasses if needed
        startGame();
    }

    public void startGame()
    {
        StartWorld world = (StartWorld) getWorld();
        world.showNameScreen();
    }
}
