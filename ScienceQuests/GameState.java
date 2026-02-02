import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * GameState - Central place to track overall game progress.
 */
public class GameState
{
    private static GameState instance;

    private final EnumSet<LabType> completedLabs;
    private final Set<String> badges;
    private final Map<String, Set<Integer>> usedQuestionIndices;
    private final Map<String, Integer> quizCorrectCount;
    private final Map<String, Integer> quizTotalCount;
    private final Random rng;
    private int xp;

    private GameState()
    {
        completedLabs = EnumSet.noneOf(LabType.class);
        badges = new HashSet<>();
        usedQuestionIndices = new HashMap<>();
        quizCorrectCount = new HashMap<>();
        quizTotalCount = new HashMap<>();
        rng = new Random();
        xp = 0;
    }

    public static GameState getInstance()
    {
        if (instance == null)
        {
            instance = new GameState();
        }
        return instance;
    }

    public void reset()
    {
        completedLabs.clear();
        badges.clear();
        usedQuestionIndices.clear();
        quizCorrectCount.clear();
        quizTotalCount.clear();
        xp = 0;
    }

    public boolean isLabCompleted(LabType lab)
    {
        return completedLabs.contains(lab);
    }

    public void setLabCompleted(LabType lab, boolean completed)
    {
        if (completed)
        {
            completedLabs.add(lab);
        }
        else
        {
            completedLabs.remove(lab);
        }
    }

    public void completeLab(LabType lab)
    {
        completedLabs.add(lab);
    }

    /**
     * Enforce lab unlock order: Biology → Physics → Chemistry
     */
    public boolean canEnterLab(LabType lab)
    {
        if (lab == LabType.BIOLOGY)
        {
            return true;
        }
        if (lab == LabType.PHYSICS)
        {
            return completedLabs.contains(LabType.BIOLOGY);
        }
        if (lab == LabType.CHEMISTRY)
        {
            return completedLabs.contains(LabType.PHYSICS);
        }
        return false;
    }

    public int getXp()
    {
        return xp;
    }

    public void addXp(int amount)
    {
        if (amount > 0)
        {
            xp += amount;
        }
    }

    public Set<String> getBadges()
    {
        return java.util.Collections.unmodifiableSet(badges);
    }

    public void awardBadge(String badgeId)
    {
        if (badgeId != null && !badgeId.trim().isEmpty())
        {
            badges.add(badgeId.trim());
        }
    }

    public void recordQuizResult(String topic, boolean correct)
    {
        String key = normalizeTopic(topic);
        quizTotalCount.put(key, quizTotalCount.getOrDefault(key, 0) + 1);
        if (correct)
        {
            quizCorrectCount.put(key, quizCorrectCount.getOrDefault(key, 0) + 1);
        }
    }

    public int getQuizCorrectCount(String topic)
    {
        return quizCorrectCount.getOrDefault(normalizeTopic(topic), 0);
    }

    public int getQuizTotalCount(String topic)
    {
        return quizTotalCount.getOrDefault(normalizeTopic(topic), 0);
    }

    /**
     * Randomly select a question from a pool without repeating in the same session.
     */
    public DialogueQuestion getRandomQuestion(String topic, List<DialogueQuestion> pool)
    {
        if (pool == null || pool.isEmpty())
        {
            return null;
        }

        String key = normalizeTopic(topic);
        Set<Integer> used = usedQuestionIndices.computeIfAbsent(key, k -> new HashSet<>());

        if (used.size() >= pool.size())
        {
            used.clear();
        }

        int index;
        int safety = 0;
        do
        {
            index = rng.nextInt(pool.size());
            safety++;
        } while (used.contains(index) && safety < 50);

        used.add(index);
        return pool.get(index);
    }

    private String normalizeTopic(String topic)
    {
        if (topic == null || topic.trim().isEmpty())
        {
            return "general";
        }
        return topic.trim().toLowerCase();
    }
}
