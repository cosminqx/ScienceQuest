import greenfoot.*;

public class StartWorld extends World
{
    private int currentScreen = 0; // 0: Title, 1: Name Input, 2: Gender Selection
    private InputField nameField;
    private GenderButton maleButton;
    private GenderButton femaleButton;
    private StartButton continueButton;
    private GreenfootSound backgroundMusic;

    public StartWorld()
    {    
        super(600, 400, 1);  // width, height, cell size
        
        // Load custom fonts once at startup (required for all dialogue)
        FontManager.loadFonts();
        
        setScaledBackground();
        
        showTitleScreen();
        prepare();
    }

    public void act()
    {
        // Check for Enter key press
        if (Greenfoot.isKeyDown("enter"))
        {
            handleEnterKey();
        }
    }

    private void handleEnterKey()
    {
        // Add a small delay to prevent double triggering
        Greenfoot.delay(5);
        
        switch(currentScreen)
        {
            case 0: // Title screen
                showNameInputScreen();
                break;
            case 1: // Name input screen
                proceedToGenderScreen();
                break;
            case 2: // Gender selection screen
                proceedToGame();
                break;
        }
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

        TitleImage title = new TitleImage();
        addObject(title, 300, 135);

        StartButton startButton = new StartButton();
        addObject(startButton, getWidth()/2, 250);
    }

    private void showNameInputScreen()
    {
        removeAllObjects();
        currentScreen = 1;

        Label title = new Label("Creaţi-vă Personajul", FontManager.getPixeledLarge(), Color.WHITE);
        addObject(title, getWidth()/2, 50);

        Label nameLabel = new Label("Introduceţi numele:", FontManager.getPixeledLarge(), Color.WHITE);
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

        Label title = new Label("Selectaţi genul:", FontManager.getPixeledLarge(), Color.WHITE);
        addObject(title, getWidth()/2, 30);

        maleButton = new GenderButton("Băiat", "spritesheet/boy/animated");
        addObject(maleButton, 175, 180);

        femaleButton = new GenderButton("Fată", "spritesheet/girl/animated");
        addObject(femaleButton, 425, 180);
        continueButton = new StartButton()
        {
            public void startGame()
            {
                proceedToGame();
            }
        };
        addObject(continueButton, getWidth()/2, 360);
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
        {
            PlayerData.setPlayerGender(Gender.BOY);
        }
        else if (femaleButton.isSelected())
        {
            PlayerData.setPlayerGender(Gender.GIRL);
        }
        else
        {
            return;
        }

        // Stop the background music before transitioning
        if (backgroundMusic != null)
        {
            backgroundMusic.stop();
        }
        
        WorldNavigator.goToMainMap();
    }

    public void showNameScreen()
    {
        showNameInputScreen();
    }
    /**
     * Prepare the world for the start of the program.
     * That is: create the initial objects and add them to the world.
     */
    private void prepare()
    {
    }

    private Font loadTitleFont()
    {
        try
        {
            // Use bundled pixel font for the title (scaled x1.5)
            return new Font("fonts/8-BIT WONDER.TTF", true, false, 54);
        }
        catch (Exception e)
        {
            // Fallback to monospaced bold if font cannot be loaded
            return new Font("Monospaced", true, false, 54);
        }
    }
}
