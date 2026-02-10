import java.util.Random;

/**
 * DialogueQuestion - Represents a multiple-choice question for dialogue
 */
public class DialogueQuestion
{
    private String topic;
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex;
    private String correctResponse;
    private String incorrectResponse;
    
    public DialogueQuestion(String questionText, String[] answers, int correctAnswerIndex,
                            String correctResponse, String incorrectResponse)
    {
        this("general", questionText, answers, correctAnswerIndex, correctResponse, incorrectResponse);
    }

    public DialogueQuestion(String topic, String questionText, String[] answers, int correctAnswerIndex,
                            String correctResponse, String incorrectResponse)
    {
        this.topic = (topic == null || topic.trim().isEmpty()) ? "general" : topic.trim();
        this.questionText = questionText;
        this.answers = answers != null ? answers : new String[0];
        this.correctAnswerIndex = Math.max(0, Math.min(correctAnswerIndex, this.answers.length - 1));
        this.correctResponse = correctResponse;
        this.incorrectResponse = incorrectResponse;
        
        // Shuffle answers and update correctAnswerIndex
        shuffleAnswers();
    }
    
    /**
     * Shuffle the answer array and update the correctAnswerIndex accordingly
     */
    private void shuffleAnswers()
    {
        if (answers.length <= 1) return;
        
        Random random = new Random();
        String correctAnswer = answers[correctAnswerIndex];
        
        // Fisher-Yates shuffle
        for (int i = answers.length - 1; i > 0; i--)
        {
            int j = random.nextInt(i + 1);
            String temp = answers[i];
            answers[i] = answers[j];
            answers[j] = temp;
        }
        
        // Find the new index of the correct answer
        for (int i = 0; i < answers.length; i++)
        {
            if (answers[i].equals(correctAnswer))
            {
                correctAnswerIndex = i;
                break;
            }
        }
    }
    
    public String getTopic()
    {
        return topic;
    }

    public String getQuestionText()
    {
        return questionText;
    }
    
    public String[] getAnswers()
    {
        return answers;
    }
    
    public int getCorrectAnswerIndex()
    {
        return correctAnswerIndex;
    }
    
    public String getCorrectResponse()
    {
        return correctResponse;
    }
    
    public String getIncorrectResponse()
    {
        return incorrectResponse;
    }
}
