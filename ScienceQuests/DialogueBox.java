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
    private String iconPath;
    
    // Multi-page dialogue support
    private ArrayList<String> pages; // Each page is separated by "---"
    private int currentPageIndex = 0; // Which page we're on (0-based)
    
    // Question mode support
    private boolean questionMode = false;
    private DialogueQuestion question;
    private int selectedIndex = 0;
    private int boxHeight = BOX_HEIGHT;
    
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
     * @param text The dialogue text to display (use "---" to separate pages)
     * @param iconPath Path to the NPC icon image (e.g., "images/man_teacher_icon.png")
     * @param useTypewriter Whether to use typewriter effect
     */
    public DialogueBox(String text, String iconPath, boolean useTypewriter)
    {
        this.fullText = text;
        this.useTypewriter = useTypewriter;
        this.typewriterSpeed = 2;
        this.currentPageIndex = 0;
        this.iconPath = iconPath;
        this.boxHeight = BOX_HEIGHT;
        
        // Load custom pixel font
        loadPixelFont();
        
        // Load icon image
        loadIcon(iconPath);
        
        // Parse pages from the full text (split by "---")
        parsePages();
        
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
     * Constructor for DialogueBox with question support
     */
    public DialogueBox(DialogueQuestion question, String iconPath, boolean useTypewriter)
    {
        this.questionMode = true;
        this.question = question;
        this.iconPath = iconPath;
        this.useTypewriter = useTypewriter;
        this.typewriterSpeed = 2;
        this.currentPageIndex = 0;
        this.boxHeight = 180; // Taller box to fit answers
        this.fullText = question != null ? question.getQuestionText() : "";
        this.selectedIndex = 0;
        
        // Load font and icon
        loadPixelFont();
        loadIcon(iconPath);
        
        // Parse pages (single page for question)
        parsePages();
        
        // Wrap question text
        wrapText();
        
        // Create image
        createDialogueBoxImage();
    }
    
    /**
     * Parse dialogue text into pages separated by "---"
     */
    private void parsePages()
    {
        pages = new ArrayList<String>();
        
        if (questionMode)
        {
            // Questions are a single page
            pages.add(fullText);
        }
        else
        {
            // Split by "---" to separate pages
            String[] pageParts = fullText.split("---");
            
            for (String page : pageParts)
            {
                // Trim whitespace from each page
                String trimmedPage = page.trim();
                if (!trimmedPage.isEmpty())
                {
                    pages.add(trimmedPage);
                }
            }
            
            // If no pages were created, add the full text as one page
            if (pages.isEmpty())
            {
                pages.add(fullText);
            }
        }
        
        System.out.println("DEBUG: Parsed " + pages.size() + " pages of dialogue");
    }
    
    /**
     * Advance to the next page of dialogue
     * @return true if there's a next page, false if we're on the last page
     */
    public boolean nextPage()
    {
        if (currentPageIndex < pages.size() - 1)
        {
            currentPageIndex++;
            displayedCharacters = 0; // Reset typewriter for new page
            wrapText();
            createDialogueBoxImage();
            System.out.println("DEBUG: Advanced to page " + (currentPageIndex + 1) + " of " + pages.size());
            return true;
        }
        return false;
    }
    
    /**
     * Check if there are more pages after the current one
     */
    public boolean hasNextPage()
    {
        return currentPageIndex < pages.size() - 1;
    }
    
    /**
     * Get the current page number (1-based)
     */
    public int getCurrentPage()
    {
        return currentPageIndex + 1;
    }
    
    /**
     * Get total number of pages
     */
    public int getTotalPages()
    {
        return pages.size();
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
        
        // Get the current page text
        String pageText = pages.isEmpty() ? fullText : pages.get(currentPageIndex);
        
        // Estimate characters per line based on box dimensions
        // With FONT_SIZE 16, approximately 45-50 characters fit per line
        int charsPerLine = 45;
        
        String[] words = pageText.split(" ");
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
        GreenfootImage boxImage = new GreenfootImage(BOX_WIDTH, boxHeight);
        boxImage.setColor(Color.BLACK);
        boxImage.fillRect(0, 0, BOX_WIDTH, boxHeight);
        
        // Draw background
        boxImage.setColor(BG_COLOR);
        boxImage.fillRect(BORDER_WIDTH, BORDER_WIDTH, 
                         BOX_WIDTH - (BORDER_WIDTH * 2), 
                         boxHeight - (BORDER_WIDTH * 2));
        
        // Draw border (pixel style - multiple rectangles for crisp edge)
        boxImage.setColor(BORDER_COLOR);
        boxImage.fillRect(0, 0, BOX_WIDTH, BORDER_WIDTH); // Top
        boxImage.fillRect(0, boxHeight - BORDER_WIDTH, BOX_WIDTH, BORDER_WIDTH); // Bottom
        boxImage.fillRect(0, 0, BORDER_WIDTH, boxHeight); // Left
        boxImage.fillRect(BOX_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, boxHeight); // Right
        
        // Draw icon if available
        if (iconImage != null)
        {
            boxImage.drawImage(iconImage, ICON_PADDING, 
                              (boxHeight - ICON_SIZE) / 2);
        }
        
        // Draw text
        if (pixelFont != null)
        {
            boxImage.setFont(pixelFont);
        }
        boxImage.setColor(TEXT_COLOR);
        
        // Draw visible text (considering typewriter effect) for the question or normal text
        int textY = PADDING + 15;
        
        if (!questionMode)
        {
            // Normal dialogue mode
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
        }
        else
        {
            // Question mode: draw question text first
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
            
            textY += 10; // spacing before answers
            
            // Draw answers with selection highlight
            if (question != null)
            {
                String[] answers = question.getAnswers();
                for (int i = 0; i < answers.length; i++)
                {
                    int optionY = textY + (i * 18);
                    int optionX = TEXT_START_X;
                    String label = (char)('A' + i) + ") ";
                    String optionText = label + answers[i];
                    
                    if (i == selectedIndex)
                    {
                        // Highlight selected option (pixel-art block)
                        boxImage.setColor(new Color(70, 70, 120));
                        boxImage.fillRect(optionX - 4, optionY - 14, 280, 18);
                        boxImage.setColor(new Color(220, 220, 255));
                    }
                    else
                    {
                        boxImage.setColor(TEXT_COLOR);
                    }
                    boxImage.drawString(optionText, optionX, optionY);
                }
            }
        }
        
        // Draw continue or instruction prompt in bottom right corner
        String continueText;
        if (questionMode)
        {
            continueText = "Alege cu 1-4 sau sageti, ENTER pentru a confirma";
        }
        else
        {
            continueText = "Apasă ENTER pentru a continua...";
        }
        boxImage.setColor(new Color(150, 150, 150)); // Slightly dimmed text
        if (pixelFont != null)
        {
            boxImage.setFont(pixelFont);
        }
        int textWidth = continueText.length() * 7;
        boxImage.drawString(continueText, BOX_WIDTH - textWidth - PADDING, boxHeight - PADDING - 5);
        
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

    /**
     * Indicates if this dialogue is a question
     */
    public boolean isQuestionMode()
    {
        return questionMode;
    }
    
    /**
     * Move selection up/down for answers
     */
    public void moveSelection(int delta)
    {
        if (!questionMode || question == null) return;
        int answerCount = question.getAnswers().length;
        selectedIndex = (selectedIndex + delta + answerCount) % answerCount;
        createDialogueBoxImage();
    }
    
    /**
     * Select a specific index (0-based)
     */
    public void selectIndex(int index)
    {
        if (!questionMode || question == null) return;
        if (index >= 0 && index < question.getAnswers().length)
        {
            selectedIndex = index;
            createDialogueBoxImage();
        }
    }
    
    /**
     * Confirm the selected answer
     * @return true if correct, false otherwise
     */
    public boolean confirmSelection()
    {
        if (!questionMode || question == null) return false;
        return selectedIndex == question.getCorrectAnswerIndex();
    }
    
    /**
     * Get the attached question
     */
    public DialogueQuestion getQuestion()
    {
        return question;
    }
    
    /**
     * Get icon path for subsequent dialogues
     */
    public String getIconPath()
    {
        return iconPath;
    }
}
