# Quick Start: Adding Dialogue to Your NPC

## Step 1: Make Your NPC Implement the NPC Interface

```java
public class MyNPC extends Actor implements NPC
{
    // ... your existing code ...
}
```

## Step 2: Add Required Methods

```java
@Override
public String getDialogueText(String playerName)
{
    return "Your dialogue here, " + playerName + "!";
}

@Override
public String getIconPath()
{
    return "images/your_icon.png"; // Icon must exist in images folder
}
```

## Step 3: Add Dialogue Detection in act()

```java
public void act()
{
    // Your existing act code...
    checkDialogueInteraction();
}

private void checkDialogueInteraction()
{
    World world = getWorld();
    if (world == null) return;
    
    // Find the player
    Actor player = null;
    if (!world.getObjects(Boy.class).isEmpty())
    {
        player = world.getObjects(Boy.class).get(0);
    }
    else if (!world.getObjects(Girl.class).isEmpty())
    {
        player = world.getObjects(Girl.class).get(0);
    }
    
    if (player != null)
    {
        // Calculate distance
        int dx = player.getX() - getX();
        int dy = player.getY() - getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Check if player is close and presses F
        if (distance < 80 && Greenfoot.isKeyDown("f"))
        {
            initiateDialogue();
        }
    }
}

private void initiateDialogue()
{
    DialogueManager manager = DialogueManager.getInstance();
    if (!manager.isDialogueActive())
    {
        String playerName = PlayerData.getPlayerName();
        DialogueBox dialogue = new DialogueBox(
            getDialogueText(playerName), 
            getIconPath()
        );
        manager.showDialogue(dialogue, getWorld(), this);
    }
}
```

## Done!

Your NPC now has dialogue. Test it by:
1. Running the game
2. Walking near your NPC (within 80 pixels)
3. Pressing **F** to trigger dialogue
4. Pressing **ENTER** to dismiss

## Optional: Add Typewriter Effect

```java
DialogueBox dialogue = new DialogueBox(
    getDialogueText(playerName), 
    getIconPath(),
    true // Enable typewriter effect
);
dialogue.setTypewriterSpeed(2); // Adjust speed (1-10)
```

## Optional: Multi-Line Dialogue

```java
@Override
public String getDialogueText(String playerName)
{
    return "First line of dialogue.\n" +
           "Second line.\n" +
           "Hello, " + playerName + "!";
}
```

## Troubleshooting

**Icon not showing?**
- Verify the icon file exists in `images/` folder
- Check exact filename in `getIconPath()`

**Font looks wrong?**
- Ensure `/fonts/Pixeled.ttf` exists
- System will fall back to Arial if not found

**Dialogue not appearing?**
- Check that MainMapWorld calls `dialogueManager.processInput()`
- Verify Teacher (or your NPC) is within 80 pixels of player
- Confirm F key is being pressed

**Multiple dialogues appearing?**
- Check that `DialogueManager.getInstance()` is being used
- The manager automatically prevents multiple dialogues
