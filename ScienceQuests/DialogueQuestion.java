/**
 * DialogueQuestion - Represents a multiple-choice question for dialogue
 */
public class DialogueQuestion
{
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex;
    private String correctResponse;
    private String incorrectResponse;
    
    public DialogueQuestion(String questionText, String[] answers, int correctAnswerIndex,
                            String correctResponse, String incorrectResponse)
    {
        this.questionText = questionText;
        this.answers = answers != null ? answers : new String[0];
        this.correctAnswerIndex = Math.max(0, Math.min(correctAnswerIndex, this.answers.length - 1));
        this.correctResponse = correctResponse;
        this.incorrectResponse = incorrectResponse;
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
