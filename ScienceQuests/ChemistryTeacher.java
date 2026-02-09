import greenfoot.*;

/**
 * ChemistryTeacher - NPC in Chemistry Lab who provides quiz questions
 */
public class ChemistryTeacher extends Actor implements NPC
{
    private LabWorld labWorld;
    private static final int INTERACTION_DISTANCE = 80;
    private boolean fKeyPressed = false;
    private int dialogueCooldown = 0;
    
    public ChemistryTeacher()
    {
        super();
    }
    
    @Override
    protected void addedToWorld(World world)
    {
        if (world instanceof LabWorld)
        {
            labWorld = (LabWorld) world;
        }
        
        // Set NPC appearance using man_teacher sprite
        GreenfootImage sprite = new GreenfootImage("man_teacher.png");
        sprite.scale(80, 80);
        setImage(sprite);
    }
    
    @Override
    public void act()
    {
        if (dialogueCooldown > 0)
        {
            dialogueCooldown--;
        }
        checkDialogueInteraction();
    }
    
    /**
     * Check for interaction (auto-trigger when nearby)
     */
    private void checkDialogueInteraction()
    {
        World world = getWorld();
        if (world == null) return;
        
        Actor player = getPlayer();
        if (player != null)
        {
            int dx = player.getX() - getX();
            int dy = player.getY() - getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < INTERACTION_DISTANCE)
            {
                // Stop showing dialogue after 5/5 quizzes complete
                GameState state = GameState.getInstance();
                if (state.getLabChemQuizTotal() >= 5)
                {
                    return;
                }
                
                DialogueManager manager = DialogueManager.getInstance();
                if (manager.isDialogueActive())
                {
                    return;
                }

                // Auto-trigger dialogue when in range and no active dialogue
                if (dialogueCooldown == 0)
                {
                    initiateChemistryDialogue();
                    dialogueCooldown = 15;
                }
            }
            else
            {
                dialogueCooldown = 0;
            }
        }
    }
    
    /**
     * Show chemistry lab quiz dialogue
     */
    private void initiateChemistryDialogue()
    {
        World world = getWorld();
        if (world == null) return;
        
        DialogueManager manager = DialogueManager.getInstance();
        
        if (manager.isDialogueActive()) return;
        
        GameState state = GameState.getInstance();
        int total = state.getLabChemQuizTotal();
        int correct = state.getLabChemQuizCorrect();

        if (total >= 5)
        {
            String doneText = "Ai terminat toate cele 5 întrebări.\n---\n" +
                "Corecte: " + correct + "/5.\n---\n" +
                "Continuă cu mini‑quest‑urile.";
            DialogueBox done = new DialogueBox(doneText, getIconPath(), true);
            done.setTypewriterSpeed(2);
            manager.showDialogue(done, world, this);
            return;
        }
        
        // Show introduction only on first question
        if (total == 0)
        {
            String introText = "Bine ai venit la Laboratorul de Chimie!\n---\n" +
                "Întrebarea 1 din 5.\n---\n" +
                "Corecte: 0/5.";
            DialogueBox intro = new DialogueBox(introText, getIconPath(), true);
            intro.setTypewriterSpeed(2);
            
            // Queue the quiz question
            DialogueQuestion question = buildChemistryQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabChemQuizResult(isCorrect);
                int t = gs.getLabChemQuizTotal();
                int c = gs.getLabChemQuizCorrect();
                DebugLog.log("Chemistry Lab Quiz Result: " + c + "/" + t + " correct");
                if (gs.isLabChemQuizGateComplete())
                {
                    DebugLog.log("CHEMISTRY MINI-QUESTS UNLOCKED! Correct: " + c + "/5");
                }
            });
            
            manager.queueDialogue(questionBox);
            manager.showDialogue(intro, world, this);
        }
        else
        {
            // Show quiz question directly for subsequent questions
            DialogueQuestion question = buildChemistryQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            // Set callback to record attempt with correctness flag
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabChemQuizResult(isCorrect);
                int t = gs.getLabChemQuizTotal();
                int c = gs.getLabChemQuizCorrect();
                DebugLog.log("Chemistry Lab Quiz Result: " + c + "/" + t + " correct");
                if (gs.isLabChemQuizGateComplete())
                {
                    DebugLog.log("CHEMISTRY MINI-QUESTS UNLOCKED! Correct: " + c + "/5");
                }
            });
            
            manager.showDialogue(questionBox, world, this);
        }
    }
    
    /**
     * Build a random chemistry question
     */
    private DialogueQuestion buildChemistryQuestion()
    {
        return GameState.getInstance().getRandomQuestion("general", QuestionPools.getGeneralScienceQuestions());
    }
    
    /**
     * Get dialogue text for NPC interface
     */
    @Override
    public String getDialogueText(String playerName)
    {
        return "Bine ai venit la Laboratorul de Chimie!";
    }
    
    /**
     * Get icon path for dialogue boxes
     */
    public String getIconPath()
    {
        return "images/man_teacher.png";
    }
    
    /**
     * Get the player (Boy or Girl)
     */
    private Actor getPlayer()
    {
        World world = getWorld();
        if (world == null) return null;
        
        if (!world.getObjects(Boy.class).isEmpty())
        {
            return world.getObjects(Boy.class).get(0);
        }
        else if (!world.getObjects(Girl.class).isEmpty())
        {
            return world.getObjects(Girl.class).get(0);
        }
        return null;
    }
}
