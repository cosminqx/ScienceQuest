import greenfoot.*;

/**
 * ChemistryTeacher - NPC in Chemistry Lab who provides quiz questions
 */
public class ChemistryTeacher extends QuizNPCBase
{
    private LabWorld labWorld;
    
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
        GreenfootImage sprite = new GreenfootImage("images/man_teacher.png");
        // Scale up by 40% from the previous 80x80 size
        sprite.scale(112, 112);
        setImage(sprite);
    }

    protected void onWorldTick(World world)
    {
        if (labWorld == null && world instanceof LabWorld)
        {
            labWorld = (LabWorld) world;
        }
    }

    protected boolean isInteractionEnabled()
    {
        GameState state = GameState.getInstance();
        return labWorld != null && !state.isLabChemQuizGateComplete();
    }

    protected void onInteract(World world)
    {
        initiateChemistryDialogue();
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

        if (state.isLabChemQuizGateComplete())
        {
            String doneText = "Ai terminat toate cele 5 întrebări.\n---\n" +
                "Corecte: " + correct + "/5.\n---\n" +
                "Continuă cu mini‑quest‑urile.";
            DialogueBox done = new DialogueBox(doneText, getIconPath(), true);
            done.setTypewriterSpeed(2);
            manager.showDialogue(done, world, this);
            return;
        }

        if (total >= 5 && correct < 3)
        {
            String retryText = "Nu ai suficiente răspunsuri corecte.\n---\n" +
                "Mai ai nevoie de " + (3 - correct) + " corecte.";
            DialogueBox retry = new DialogueBox(retryText, getIconPath(), true);
            retry.setTypewriterSpeed(2);

            DialogueQuestion question = buildChemistryQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabChemQuizResult(isCorrect);
            });

            manager.queueDialogue(questionBox);
            manager.showDialogue(retry, world, this);
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
            
            DialogueQuestion question = buildChemistryQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabChemQuizResult(isCorrect);
            });
            
            manager.queueDialogue(questionBox);
            manager.showDialogue(intro, world, this);
        }
        else
        {
            DialogueQuestion question = buildChemistryQuestion();
            DialogueBox questionBox = new DialogueBox(question, getIconPath(), true);
            questionBox.setTypewriterSpeed(2);
            
            // Set callback to record attempt with correctness flag
            questionBox.setOnAnswerAttemptCallback(isCorrect -> {
                GameState gs = GameState.getInstance();
                gs.recordLabChemQuizResult(isCorrect);
            });
            
            manager.showDialogue(questionBox, world, this);
        }
    }
    
    /**
     * Build a random chemistry question
     */
    private DialogueQuestion buildChemistryQuestion()
    {
        return GameState.getInstance().getRandomQuestion("chemistry", QuestionPools.getChemistryQuestions());
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
}
