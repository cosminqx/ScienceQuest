import greenfoot.*;
import java.util.ArrayList;

/**
 * DialogueBox
 * Displays dialogue text with pixel font support, multi-page support, and question mode
 */
public class DialogueBox extends Actor {

    private static final int BOX_WIDTH = 500;
    private static final int BOX_HEIGHT = 120;
    private static final int PADDING = 10;
    private static final int ICON_SIZE = 80;
    private static final int ICON_PADDING = 10;
    private static final int TEXT_START_X =
            ICON_SIZE + (ICON_PADDING * 2) + PADDING;

    private static final Color BG_COLOR = new Color(30, 30, 30);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int BORDER_WIDTH = 3;

    private String fullText;
    private ArrayList<String> wrappedLines;
    private GreenfootImage iconImage;
    private String iconPath;

    private boolean useTypewriter;
    private int displayedCharacters = 0;
    private int typewriterCounter = 0;
    private int typewriterSpeed = 2;
    
    // Multi-page support
    private ArrayList<String> pages;
    private int currentPageIndex = 0;
    
    // Question mode support
    private boolean questionMode = false;
    private DialogueQuestion question;
    private int selectedIndex = 0;
    private int boxHeight = BOX_HEIGHT;
    
    // Callback for correct answer
    private Runnable onCorrectAnswerCallback;
    // Callback for any answer attempt with correctness flag
    private java.util.function.Consumer<Boolean> onAnswerAttemptCallback;

    public DialogueBox(String text, String iconPath, boolean typewriter) {
        this.fullText = text != null ? text : "";
        this.iconPath = iconPath;
        this.useTypewriter = typewriter;
        this.questionMode = false;
        this.boxHeight = BOX_HEIGHT;

        loadIcon(iconPath);
        parsePages();
        wrapText();
        createImage();
    }

    public DialogueBox(String text, String iconPath) {
        this(text, iconPath, false);
    }
    
    // Question constructor
    public DialogueBox(DialogueQuestion question, String iconPath, boolean typewriter) {
        this.question = question;
        this.iconPath = iconPath;
        this.useTypewriter = typewriter;
        this.questionMode = true;
        this.fullText = question != null ? question.getQuestionText() : "";
        this.boxHeight = 180;
        this.selectedIndex = 0;
        
        loadIcon(iconPath);
        parsePages();
        wrapText();
        createImage();
    }
    
    private void parsePages() {
        pages = new ArrayList<>();
        if (questionMode) {
            pages.add(fullText);
        } else {
            String[] parts = fullText.split("---");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    pages.add(trimmed);
                }
            }
            if (pages.isEmpty()) {
                pages.add(fullText);
            }
        }
    }

    private void loadIcon(String path) {
        try {
            iconImage = new GreenfootImage(path);
            iconImage.scale(ICON_SIZE, ICON_SIZE);
        } catch (Exception e) {
            iconImage = new GreenfootImage(ICON_SIZE, ICON_SIZE);
            iconImage.setColor(Color.GRAY);
            iconImage.fill();
        }
    }

    private void wrapText() {
        wrappedLines = new ArrayList<String>();
        
        String pageText = pages.isEmpty() ? fullText : pages.get(currentPageIndex);
        int charsPerLine = 45;
        String[] words = pageText.split(" ");
        String line = "";

        for (String w : words) {
            String test = line.isEmpty() ? w : line + " " + w;

            if (test.length() > charsPerLine) {
                wrappedLines.add(line);
                line = w;
            } else {
                line = test;
            }
        }

        if (!line.isEmpty()) wrappedLines.add(line);
    }

    private void createImage() {
        GreenfootImage img = new GreenfootImage(BOX_WIDTH, boxHeight);

        img.setColor(Color.BLACK);
        img.fill();

        img.setColor(BG_COLOR);
        img.fillRect(
                BORDER_WIDTH,
                BORDER_WIDTH,
                BOX_WIDTH - BORDER_WIDTH * 2,
                boxHeight - BORDER_WIDTH * 2
        );

        img.setColor(BORDER_COLOR);
        img.fillRect(0, 0, BOX_WIDTH, BORDER_WIDTH);
        img.fillRect(0, boxHeight - BORDER_WIDTH, BOX_WIDTH, BORDER_WIDTH);
        img.fillRect(0, 0, BORDER_WIDTH, boxHeight);
        img.fillRect(BOX_WIDTH - BORDER_WIDTH, 0, BORDER_WIDTH, boxHeight);

        if (iconImage != null) {
            img.drawImage(iconImage, ICON_PADDING,
                    (boxHeight - ICON_SIZE) / 2);
        }

        img.setFont(FontManager.getPixeled());
        img.setColor(TEXT_COLOR);

        int y = PADDING + 20;

        if (!questionMode) {
            // Normal dialogue mode
            if (!useTypewriter) {
                for (String line : wrappedLines) {
                    img.drawString(line, TEXT_START_X, y);
                    y += 20;
                }
            } else {
                int chars = 0;
                for (String line : wrappedLines) {
                    int len = Math.min(
                            displayedCharacters - chars,
                            line.length()
                    );
                    if (len > 0) {
                        img.drawString(line.substring(0, len),
                                TEXT_START_X, y);
                    }
                    chars += line.length() + 1;
                    y += 20;
                    if (chars >= displayedCharacters) break;
                }
            }
        } else {
            // Question mode
            if (!useTypewriter) {
                for (String line : wrappedLines) {
                    img.drawString(line, TEXT_START_X, y);
                    y += 20;
                }
            } else {
                int chars = 0;
                for (String line : wrappedLines) {
                    int len = Math.min(
                            displayedCharacters - chars,
                            line.length()
                    );
                    if (len > 0) {
                        img.drawString(line.substring(0, len),
                                TEXT_START_X, y);
                    }
                    chars += line.length() + 1;
                    y += 20;
                    if (chars >= displayedCharacters) break;
                }
            }
            
            y += 10;
            
            if (question != null) {
                String[] answers = question.getAnswers();
                img.setFont(new greenfoot.Font("Arial", true, true, 10));
                for (int i = 0; i < answers.length; i++) {
                    int optionY = y + (i * 18);
                    String label = (char)('A' + i) + ") ";
                    String optionText = label + answers[i];
                    
                    if (i == selectedIndex) {
                        img.setColor(new Color(70, 70, 120));
                        img.fillRect(TEXT_START_X - 4, optionY - 14, 280, 18);
                        img.setColor(new Color(220, 220, 255));
                    } else {
                        img.setColor(TEXT_COLOR);
                    }
                    img.drawString(optionText, TEXT_START_X, optionY);
                }
            }
        }

        img.setFont(FontManager.getPixeledSmall());
        img.setColor(new Color(150,150,150));
        String promptText = questionMode
                ? "1-4/↑↓: alege | ENTER: confirmă | ESC: închide"
                : "ENTER: continuă | ESC: închide";
        img.drawString(promptText, BOX_WIDTH - 260, boxHeight - 10);

        setImage(img);
    }

    public void act() {
        if (useTypewriter && displayedCharacters < fullText.length()) {
            typewriterCounter++;
            if (typewriterCounter >= typewriterSpeed) {
                typewriterCounter = 0;
                displayedCharacters++;
                createImage();
            }
        }
    }

    public void skip() {
        displayedCharacters = fullText.length();
        createImage();
    }
    
    public void skipTypewriter() {
        skip();
    }
    
    // Multi-page support
    public boolean nextPage() {
        if (questionMode) return false;
        
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            displayedCharacters = 0;
            wrapText();
            createImage();
            return true;
        }
        return false;
    }
    
    public boolean hasNextPage() {
        return !questionMode && currentPageIndex < pages.size() - 1;
    }
    
    public boolean isFullyDisplayed() {
        return !useTypewriter || displayedCharacters >= fullText.length();
    }
    
    // Question mode support
    public boolean isQuestionMode() {
        return questionMode;
    }
    
    public void moveSelection(int delta) {
        if (!questionMode || question == null) return;
        int count = question.getAnswers().length;
        selectedIndex = (selectedIndex + delta + count) % count;
        createImage();
    }
    
    public void selectIndex(int index) {
        if (!questionMode || question == null) return;
        if (index >= 0 && index < question.getAnswers().length) {
            selectedIndex = index;
            createImage();
        }
    }
    
    public boolean confirmSelection() {
        if (!questionMode || question == null) return false;
        boolean isCorrect = selectedIndex == question.getCorrectAnswerIndex();
        
        // Record quiz result in GameState
        GameState state = GameState.getInstance();
        state.recordQuizResult(question.getTopic(), isCorrect);
        
        // Call attempt callback with correctness flag
        if (onAnswerAttemptCallback != null)
        {
            onAnswerAttemptCallback.accept(isCorrect);
        }
        
        if (isCorrect)
        {
            // Award XP for correct answer
            state.addXp(10);
            
            // Trigger callback if answer is correct
            if (onCorrectAnswerCallback != null)
            {
                onCorrectAnswerCallback.run();
            }
        }
        
        return isCorrect;
    }
    
    public DialogueQuestion getQuestion() {
        return question;
    }
    
    public String getIconPath() {
        return iconPath;
    }
    
    public String getFullText() {
        return fullText;
    }
    
    public void setTypewriterSpeed(int speed) {
        this.typewriterSpeed = Math.max(1, speed);
    }
    
    public void setOnCorrectAnswerCallback(Runnable callback) {
        this.onCorrectAnswerCallback = callback;
    }
    
    public void setOnAnswerAttemptCallback(java.util.function.Consumer<Boolean> callback) {
        this.onAnswerAttemptCallback = callback;
    }

    public DialogueBox createRetryBox() {
        if (!questionMode || question == null) {
            return null;
        }
        DialogueBox retry = new DialogueBox(question, iconPath, true);
        retry.setTypewriterSpeed(typewriterSpeed);
        retry.setOnCorrectAnswerCallback(onCorrectAnswerCallback);
        retry.setOnAnswerAttemptCallback(onAnswerAttemptCallback);
        return retry;
    }
}