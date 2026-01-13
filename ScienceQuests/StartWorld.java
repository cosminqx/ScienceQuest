import greenfoot.*;

public class StartWorld extends World
{
    public StartWorld()
    {    
        super(600, 400, 1);  // width, height, cell size
        prepare();
    }

    private void prepare()
    {
        // Title
        Label title = new Label("ScienceQuests", 48);
        addObject(title, getWidth()/2, 120);

        // Start Button
        StartButton startButton = new StartButton();
        addObject(startButton, getWidth()/2, 250);
    }
}
