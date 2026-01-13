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
        showTitleScreen();
    }

    private void showTitleScreen()
    {
        removeAllObjects();
        currentScreen = 0;

        // Title
        Label title = new Label("ScienceQuests", 48);
        addObject(title, getWidth()/2, 120);

        // Start Button
        StartButton startButton = new StartButton();
        addObject(startButton, getWidth()/2, 250);
    }

    private void showNameInputScreen()
    {
        removeAllObjects();
        currentScreen = 1;

        // Title
        Label title = new Label("Create Your Character", 36);
        addObject(title, getWidth()/2, 50);

        // Name label
        Label nameLabel = new Label("Enter your name:", 24);
        addObject(nameLabel, getWidth()/2, 130);

        // Name input field
        nameField = new InputField(300, 40, 20);
        addObject(nameField, getWidth()/2, 180);

        // Continue button
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

        // Title
        Label title = new Label("Select your gender:", 36);
        addObject(title, getWidth()/2, 50);

        // Gender buttons
        maleButton = new GenderButton("Male");
        addObject(maleButton, 150, 180);

        femaleButton = new GenderButton("Female");
        addObject(femaleButton, getWidth()/2, 180);

        otherButton = new GenderButton("Other");
        addObject(otherButton, getWidth() - 150, 180);

        // Continue button
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
        if (maleButton != null && femaleButton != null && otherButton != null)
        {
            if (maleButton.isSelected())
            {
                PlayerData.setPlayerGender("Male");
            }
            else if (femaleButton.isSelected())
            {
                PlayerData.setPlayerGender("Female");
            }
            else if (otherButton.isSelected())
            {
                PlayerData.setPlayerGender("Other");
            }
            else
            {
                return; // No gender selected
            }

            Greenfoot.setWorld(new MainMapWorld());
        }
    }

    public void showNameScreen()
    {
        showNameInputScreen();
    }
}
