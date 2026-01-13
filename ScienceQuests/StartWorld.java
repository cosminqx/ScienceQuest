import greenfoot.*;

public class StartWorld extends World
{
    private int currentScreen = 0; // 0: Title, 1: Name Input, 2: Gender Selection
    private InputField nameField;
    private GenderButton maleButton;
    private GenderButton femaleButton;
    private GenderButton otherButton;
    private StartButton continueButton;

    public StartWorld()
    {    
        super(600, 400, 1);  // width, height, cell size
        setScaledBackground();
        showTitleScreen();
    }

    /**
     * Scale and set the background image to fit the world
     */
    private void setScaledBackground()
    {
        GreenfootImage bg =
            new GreenfootImage("pngtree-nobody-interface-of-pixel-game-platform-picture-image_1962988.jpg");
        bg.scale(getWidth(), getHeight());
        setBackground(bg);
    }

    /**
     * Remove all objects from the world
     */
    private void removeAllObjects()
    {
        for (Actor actor : getObjects(Actor.class))
        {
            removeObject(actor);
        }
    }

    private void showTitleScreen()
    {
        removeAllObjects();
        currentScreen = 0;

        Label title = new Label("ScienceQuests", 48);
        addObject(title, getWidth()/2, 120);

        StartButton startButton = new StartButton();
        addObject(startButton, getWidth()/2, 250);
    }

    private void showNameInputScreen()
    {
        removeAllObjects();
        currentScreen = 1;

        Label title = new Label("Create Your Character", 36);
        addObject(title, getWidth()/2, 50);

        Label nameLabel = new Label("Enter your name:", 24);
        addObject(nameLabel, getWidth()/2, 130);

        nameField = new InputField(300, 40, 20);
        addObject(nameField, getWidth()/2, 180);

        continueButton = new StartButton() 
        {
            public void startGame()
            {
                proceedToGenderScreen();
            }
        };
        addObject(continueButton, getWidth()/2, 280);
    }

    private void showGenderSelectionScreen()
    {
        removeAllObjects();
        currentScreen = 2;

        Label title = new Label("Select your gender:", 36);
        addObject(title, getWidth()/2, 50);

        maleButton = new GenderButton("Male");
        addObject(maleButton, 150, 180);

        femaleButton = new GenderButton("Female");
        addObject(femaleButton, getWidth()/2, 180);

        otherButton = new GenderButton("Other");
        addObject(otherButton, getWidth() - 150, 180);

        continueButton = new StartButton()
        {
            public void startGame()
            {
                proceedToGame();
            }
        };
        addObject(continueButton, getWidth()/2, 300);
    }

    private void proceedToGenderScreen()
    {
        if (nameField != null && !nameField.getText().trim().isEmpty())
        {
            PlayerData.setPlayerName(nameField.getText());
            showGenderSelectionScreen();
        }
    }

    private void proceedToGame()
    {
        if (maleButton.isSelected())
            PlayerData.setPlayerGender("Male");
        else if (femaleButton.isSelected())
            PlayerData.setPlayerGender("Female");
        else if (otherButton.isSelected())
            PlayerData.setPlayerGender("Other");
        else
            return;

        Greenfoot.setWorld(new MainMapWorld());
    }

    public void showNameScreen()
    {
        showNameInputScreen();
    }
}
