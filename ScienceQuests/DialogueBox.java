import greenfoot.*;
import java.util.ArrayList;

/**
 * DialogueBox - Displays dialogue for NPCs at the bottom of the screen
 * Features:
 * - Displays dialogue text with custom pixel font
 * - Shows NPC icon on the left side
 * - Auto-wrapping text for multiple lines
 * - Optional typewriter effect
 * - Pixel-art style with simple border
 */
public class DialogueBox extends Actor
{
    // Layout constants
    private static final int BOX_WIDTH = 500;
    private static final int BOX_HEIGHT = 120;
    private static final int PADDING = 10;
    private static final int ICON_SIZE = 80;
    private static final int ICON_PADDING = 10;
    private static final int TEXT_START_X = ICON_SIZE + (ICON_PADDING * 2) + PADDING;
    
    // Colors
    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color TEXT_COLOR = new Color(255, 255, 255);
    private static final int BORDER_WIDTH = 3;
    
    // Dialogue content
    private String fullText;
    private ArrayList<String> wrappedLines;
    private GreenfootImage iconImage;
    
    // Typewriter effect
    private boolean useTypewriter;
    private int displayedCharacters = 0;
    private int typewriterSpeed = 2; // acts between character displays
    private int typewriterCounter = 0;
    
    // Font
    private Font pixelFont;
    private static final int FONT_SIZE = 16;
    
    /**
     * Constructor for DialogueBox
     * @param text The dialogue text to display
     * @param iconPath Path to the NPC icon image (e.g., "images/man_teacher_icon.png")
     * @param useTypewriter Whether to use typewriter effect
     */
    public DialogueBox(String text, String iconPath, boolean useTypewriter)
    {
        this.fullText = text;
        this.useTypewriter = useTypewriter;
        this.typewriterSpeed = 2;
        
        // Load custom pixel font
        loadPixelFont();
        
        // Load icon image
        loadIcon(iconPath);
        
        // Wrap text to fit the dialogue box
        wrapText();
        
        // Create the dialogue box image
        createDialogueBoxImage();
    }
    
    /**
     * Constructor for DialogueBox without typewriter effect (default)
     */
    public DialogueBox(String text, String iconPath)
    {
        this(text, iconPath, false);
    }
    
    /**
     * Load the custom pixel font from the fonts folder
     * Note: Greenfoot's Font class only supports size specification
     */
    private void loadPixelFont()
    {
        try
        {
            // Create Greenfoot font with specified size
            // Greenfoot will use default system font at this size
            pixelFont = new Font(FONT_SIZE);
        }
        catch (Exception e)
        {
            System.out.println("WARNING: Could not create font, using default");
            // Fallback to default size
            pixelFont = new Font(FONT_SIZE);
        }
    }
    
    /**
     * Load the NPC icon image
     */
    private void loadIcon(String iconPath)
    {
        try
        {
            iconImage = new GreenfootImage(iconPath);
            // Scale icon to fit the allocated space
            iconImage.scale(ICON_SIZE, ICON_SIZE);
        }
        catch (Exception e)
        {
            System.out.println("ERROR loading icon: " + e.getMessage());
            // Create a placeholder icon
            iconImage = new GreenfootImage(ICON_SIZE, ICON_SIZE);
            iconImage.setColor(new Color(100, 100, 100));
            iconImage.fillRect(0, 0, ICON_SIZE, ICON_SIZE);
        }
    }
    
    /**
     * Wrap text to fit within the dialogue box width
     */
    private void wrapText()
    {
        wrappedLines = new ArrayList<String>();
        
        // Estimate characters per line based on box dimensions
        // With FONT_SIZE 16, approximately 45-50 characters fit per line
        int charsPerLine = 45;
        
        String[] words = fullText.split(" ");
        String currentLine = "";
        
        for (String word : words)
        {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            
            // If adding this word exceeds the character limit and we have a current line
            if (testLine.length() > charsPerLine && !currentLine.isEmpty())
            {
                wrappedLines.add(currentLine);
                currentLine = word;
            }
            else
            {
                currentLine = testLine;
            }
        }
        
        // Add the last line if it has content
        if (!currentLine.isEmpty())
        {
            wrappedLines.add(currentLine);
        }
    }
    
    /**
     * Create the dialogue box image
     */
    private void createDialogueBoxImage()
    {
        GreenfootImage boxImage = new GreenfootImage(BOX_WIDTH, BOX_HEIGHT);
        boxImage.setColor(Color.BLACK);
        boxImage.fillRect(0, 0, BOX_WIDTH, BOX_HEIGHT);
        
        // Draw background
        boxImage.setColor(BG_COLOR);
        boxImage.fillRect(BORDER_WIDTH, BORDER_WIDTH, 
                         BOX_WIDTH - (BORDER_WIDTH * 2), 
                         BOX_HEIGHT - (BORDER_WIDTH * 2));
        
        // Draw border (pixel style - multiple rectangles for crisp edge)
        boxImage.setColor(BORDER_COLOR);
        boxImage.fillRect(0, 0, BOX_WIDTH, BORDER_WIDTH); // Top
        boxImage.fillRect(0, BOX_HEIGHT - BORDER_WIDTH, BOX_WIDTH, BORDER_WIDTH); // Bottom
        boxImage.fillRect(0, 0, BORDER_WIDTH, BOX_HEIGHT); // Left
        boxImage.fillRect(BOX_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, BOX_HEIGHT); // Right
        
        // Draw icon if available
        if (iconImage != null)
        {
            boxImage.drawImage(iconImage, ICON_PADDING, 
                              (BOX_HEIGHT - ICON_SIZE) / 2);
        }
        
        // Draw text
        if (pixelFont != null)
        {
            boxImage.setFont(pixelFont);
        }
        boxImage.setColor(TEXT_COLOR);
        
        // Draw visible text (considering typewriter effect)
        int textY = PADDING + 15;
        String textToDraw = useTypewriter ? 
                           getTypewriterText() : 
                           fullText;
        
        // If using simple display, draw all wrapped lines
        if (!useTypewriter)
        {
            for (String line : wrappedLines)
            {
                boxImage.drawString(line, TEXT_START_X, textY);
                textY += 20;
            }
        }
        else
        {
            // For typewriter, rebuild from wrapped lines
            int charsDrawn = 0;
            for (String line : wrappedLines)
            {
                int lineEnd = Math.min(displayedCharacters - charsDrawn, line.length());
                String visibleLine = line.substring(0, lineEnd);
                boxImage.drawString(visibleLine, TEXT_START_X, textY);
                textY += 20;
                charsDrawn += line.length() + 1; // +1 for space
                
                if (charsDrawn >= displayedCharacters)
                {
                    break;
                }
            }
        }
        
        // Draw continue prompt in bottom right corner
        String continueText = "Apasă ENTER pentru a continua...";
        boxImage.setColor(new Color(150, 150, 150)); // Slightly dimmed text
        if (pixelFont != null)
        {
            boxImage.setFont(pixelFont);
        }
        // Estimate text width (rough calculation: ~8 pixels per character at font size 16)
        int textWidth = continueText.length() * 7;
        boxImage.drawString(continueText, BOX_WIDTH - textWidth - PADDING, BOX_HEIGHT - PADDING - 5);
        
        setImage(boxImage);
    }
    
    /**
     * Get the text to display with typewriter effect
     */
    private String getTypewriterText()
    {
        if (displayedCharacters >= fullText.length())
        {
            return fullText;
        }
        return fullText.substring(0, displayedCharacters);
    }
    
    /**
     * Update the dialogue box image (for typewriter effect)
     */
    public void act()
    {
        if (useTypewriter && displayedCharacters < fullText.length())
        {
            typewriterCounter++;
            if (typewriterCounter >= typewriterSpeed)
            {
                typewriterCounter = 0;
                displayedCharacters++;
                createDialogueBoxImage();
            }
        }
    }
    
    /**
     * Check if the dialogue is fully displayed
     */
    public boolean isFullyDisplayed()
    {
        return !useTypewriter || displayedCharacters >= fullText.length();
    }
    
    /**
     * Get the full text of this dialogue
     */
    public String getFullText()
    {
        return fullText;
    }
    
    /**
     * Set the typewriter speed
     */
    public void setTypewriterSpeed(int speed)
    {
        this.typewriterSpeed = Math.max(1, speed);
    }
}
