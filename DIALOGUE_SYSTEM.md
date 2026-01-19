# Dialogue System Documentation

## Overview
A complete dialogue system for the ScienceQuest game featuring pixel-art style dialogue boxes with NPC icons, custom fonts, and automatic text wrapping.

## Components

### 1. DialogueBox (DialogueBox.java)
The main visual component that displays dialogue on screen.

**Features:**
- Extends `Actor` for integration with Greenfoot
- Displays dialogue text with custom pixel font (Pixeled.ttf)
- Shows NPC icon on the left side of the dialogue box
- Automatic text wrapping for multi-line dialogue
- Optional typewriter effect for gradual text reveal
- Positioned at the bottom center of the screen
- Pixel-art style with simple colored border

**Key Methods:**
```java
// Create a dialogue box
DialogueBox dialogue = new DialogueBox(text, iconPath, useTypewriter);

// Check if dialogue is fully displayed
boolean isComplete = dialogue.isFullyDisplayed();

// Set typewriter speed (1-based, lower = faster)
dialogue.setTypewriterSpeed(2);
```

**Layout:**
- Box Width: 500 pixels
- Box Height: 120 pixels
- Icon Size: 80x80 pixels
- Text Area: Fills remaining space with auto-wrapping
- Border: 3 pixels thick for crisp pixel-art style
- Colors: Dark gray background (#1E1E1E), light gray border (#C8C8C8), white text

### 2. DialogueManager (DialogueManager.java)
Global manager for dialogue state and input handling using Singleton pattern.

**Features:**
- Prevents multiple dialogue boxes from displaying simultaneously
- Handles ENTER key for dismissing dialogue
- Tracks which NPC initiated the dialogue
- Thread-safe singleton implementation

**Key Methods:**
```java
// Get the singleton instance
DialogueManager manager = DialogueManager.getInstance();

// Show a dialogue
DialogueBox dialogue = new DialogueBox(...);
manager.showDialogue(dialogue, world, npcReference);

// Hide the current dialogue
manager.hideDialogue();

// Check if dialogue is active
if (manager.isDialogueActive()) { ... }

// Process input (called from World.act())
manager.processInput();

// Reset when changing worlds
manager.reset();
```

### 3. NPC Interface (NPC.java)
Interface that all NPCs must implement to support dialogue.

**Methods:**
```java
// Get dialogue text for this NPC
String getDialogueText(String playerName);

// Get icon path for dialogue box
String getIconPath();
```

### 4. Teacher Class (Updated)
The Teacher NPC now implements the NPC interface and handles dialogue activation.

**Dialogue Activation:**
- Press **F** key when near the Teacher
- Interaction range: 80 pixels
- Shows "Hello there, [PlayerName]!" dialogue
- Icon: `images/man_teacher_icon.png`

**Key Methods:**
```java
// Called automatically by Greenfoot
@Override
public void act()

// Retrieve dialogue text
@Override
public String getDialogueText(String playerName)

// Retrieve icon path
@Override
public String getIconPath()
```

### 5. MainMapWorld Class (Updated)
The main game world now integrates the dialogue system.

**Integration:**
- Initializes `DialogueManager` singleton in constructor
- Calls `dialogueManager.processInput()` in `act()` method
- Handles ENTER key dismissal of dialogue

## Usage Guide

### Basic Implementation

1. **For existing NPCs:**
```java
public class YourNPC extends Actor implements NPC
{
    @Override
    public String getDialogueText(String playerName)
    {
        return "Hello, " + playerName + "!";
    }
    
    @Override
    public String getIconPath()
    {
        return "images/your_icon.png";
    }
    
    public void act()
    {
        // Your NPC logic here
        checkDialogueInteraction();
    }
    
    private void checkDialogueInteraction()
    {
        // Check distance to player and F key press
        // Then call initiateDialogue()
    }
    
    private void initiateDialogue()
    {
        DialogueManager manager = DialogueManager.getInstance();
        if (!manager.isDialogueActive())
        {
            String playerName = PlayerData.getPlayerName();
            DialogueBox dialogue = new DialogueBox(
                getDialogueText(playerName), 
                getIconPath(), 
                false // or true for typewriter effect
            );
            manager.showDialogue(dialogue, getWorld(), this);
        }
    }
}
```

2. **Multiple Dialogue Lines:**
```java
// Create dialogue with newlines for multiple sections
String dialogue = "First line here.\nSecond line here.\nThird line here.";
DialogueBox box = new DialogueBox(dialogue, iconPath);
```

3. **With Typewriter Effect:**
```java
DialogueBox dialogue = new DialogueBox(text, iconPath, true); // true for typewriter
dialogue.setTypewriterSpeed(2); // Adjust speed
```

## Player Interaction

1. **Trigger Dialogue:**
   - Move near an NPC (within 80 pixels for Teacher)
   - Press **F** key
   - Dialogue box appears at bottom center of screen

2. **Dismiss Dialogue:**
   - Press **ENTER** key
   - Dialogue box disappears
   - Can interact with NPC again immediately

## Customization

### Change Dialogue Text
In the NPC class's `getDialogueText()` method:
```java
@Override
public String getDialogueText(String playerName)
{
    if ("custom_condition") {
        return "Custom dialogue";
    }
    return "Default dialogue for " + playerName;
}
```

### Change Icon
In the NPC class's `getIconPath()` method:
```java
@Override
public String getIconPath()
{
    return "images/your_custom_icon.png";
}
```

### Adjust Interaction Distance
In the NPC class:
```java
private static final int INTERACTION_DISTANCE = 100; // Increase from 80
```

### Modify Dialogue Box Appearance
In DialogueBox class constants:
```java
private static final Color BG_COLOR = new Color(50, 50, 50); // Lighter
private static final Color BORDER_COLOR = new Color(150, 150, 150);
private static final Color TEXT_COLOR = new Color(200, 255, 200); // Green text
```

## File Requirements

**Images:**
- `images/man_teacher_icon.png` - Teacher's dialogue icon
- Any other NPC icons in the `images/` folder

**Fonts:**
- `/fonts/Pixeled.ttf` - Custom pixel font (required)
- Fallback to Arial if font not found

## Technical Details

### Text Wrapping Algorithm
- Uses actual font metrics from custom Pixeled.ttf font
- Words are wrapped to fit within the dialogue box width
- Maximum line width accounts for icon and padding

### Font Handling
- Loads TTF font from `fonts/Pixeled.ttf`
- Falls back to Arial if custom font unavailable
- Enables crisp, pixel-art aesthetic

### Performance Considerations
- DialogueBox only redraws when typewriter effect updates
- DialogueManager uses efficient singleton pattern
- No memory leaks from dialogue boxes (removed from world on dismiss)

## Known Limitations

1. **Font Rendering:** Anti-aliasing is applied by Java's font rendering. For true pixel-perfect text, consider using pre-rendered text images.
2. **Line Count:** Designed for 2-4 lines of dialogue. Very long text may overflow the box.
3. **Multiple Dialogues:** Only one dialogue can be active at a time. Future enhancement could support dialogue branching.

## Future Enhancement Ideas

1. **Dialogue Trees:** Multiple choice selections
2. **Animations:** Slide-in/slide-out animations
3. **Portraits:** Larger NPC portraits instead of small icons
4. **Voice:** Audio playback synchronized with typewriter effect
5. **Localization:** Multi-language support
6. **State Tracking:** Remember which NPCs player has talked to

## Testing Checklist

- [x] Compile without errors
- [x] Dialogue appears on F key press near Teacher
- [x] Dialogue displays player name correctly
- [x] Dialogue disappears on ENTER key press
- [x] Multiple F presses don't spawn multiple dialogues
- [x] Icon displays correctly
- [x] Text wraps to multiple lines
- [x] Custom font loads and displays
- [x] Dialogue positioned at bottom center
- [ ] Test with actual player walkthrough
