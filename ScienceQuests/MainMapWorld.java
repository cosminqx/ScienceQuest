import greenfoot.*;

public class MainMapWorld extends World
{
    public MainMapWorld()
    {
        super(600, 400, 1);

        Label label = new Label("Main Map (Prototype)", 30);
        addObject(label, getWidth()/2, getHeight()/2);
    }
}
