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
    private int level;
    private boolean miniQuestActive;
    private static final int MAX_XP = 100; // Max XP before leveling up
    
    // Game progression tracking
    private int mainMapNPCCorrectCount = 0;        // Correct answers from Teacher in MainMapWorld
    private int mainMapNPCTotalCount = 0;          // Total questions asked from Teacher in MainMapWorld
    private static final int NPC_QUIZ_LIMIT = 5;   // Max 5 quizzes per NPC in MainMapWorld
    private static final int CORRECT_NEEDED = 3;   // Need 3 correct to unlock quests
    private boolean mainMapQuestsUnlocked = false; // Whether quests are unlocked after 3/5 correct
    private int labNPCCorrectCountBio = 0;         // Correct answers from NPC in LabBiologyWorld
    private int labNPCTotalCountBio = 0;           // Total questions in LabBiologyWorld
    private int labNPCCorrectCountPhys = 0;        // Correct answers from NPC in LabFizicaWorld
    private int labNPCTotalCountPhys = 0;          // Total questions in LabFizicaWorld
    private int labNPCCorrectCountChem = 0;        // Correct answers from NPC in LabWorld (Chemistry)
    private int labNPCTotalCountChem = 0;          // Total questions in LabWorld (Chemistry)
    private boolean hasShownMainMapTutorial = false; // Track if tutorial was shown in MainMapWorld
    
    // MainMap mini-quest completion tracking
    private boolean rapidFireQuestComplete = false;
    private boolean keySequenceQuestComplete = false;
    private boolean alternatingKeysQuestComplete = false;
    private boolean doubleTapSprintQuestComplete = false;
    private boolean comboChainQuestComplete = false;
    private boolean directionDodgeQuestComplete = false;

    private GameState()
    {
        completedLabs = EnumSet.noneOf(LabType.class);
        badges = new HashSet<>();
        usedQuestionIndices = new HashMap<>();
        quizCorrectCount = new HashMap<>();
        quizTotalCount = new HashMap<>();
        rng = new Random();
        xp = 0;
        level = 1;
        miniQuestActive = false;
        mainMapNPCCorrectCount = 0;
        mainMapNPCTotalCount = 0;
        mainMapQuestsUnlocked = false;
        labNPCCorrectCountBio = 0;
        labNPCTotalCountBio = 0;
        labNPCCorrectCountPhys = 0;
        labNPCTotalCountPhys = 0;
        labNPCCorrectCountChem = 0;
        labNPCTotalCountChem = 0;
        hasShownMainMapTutorial = false;
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
        level = 1;
        miniQuestActive = false;
        mainMapNPCCorrectCount = 0;
        mainMapNPCTotalCount = 0;
        mainMapQuestsUnlocked = false;
        labNPCCorrectCountBio = 0;
        labNPCTotalCountBio = 0;
        labNPCCorrectCountPhys = 0;
        labNPCTotalCountPhys = 0;
        labNPCCorrectCountChem = 0;
        labNPCTotalCountChem = 0;
        hasShownMainMapTutorial = false;
        rapidFireQuestComplete = false;
        keySequenceQuestComplete = false;
        alternatingKeysQuestComplete = false;
        doubleTapSprintQuestComplete = false;
        comboChainQuestComplete = false;
        directionDodgeQuestComplete = false;
    }

    public boolean hasShownMainMapTutorial()
    {
        return hasShownMainMapTutorial;
    }

    public void setMainMapTutorialShown()
    {
        hasShownMainMapTutorial = true;
    }

    public boolean isMiniQuestActive()
    {
        return miniQuestActive;
    }

    public void setMiniQuestActive(boolean active)
    {
        this.miniQuestActive = active;
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

    public int getMaxXP()
    {
        return MAX_XP;
    }

    public void addXp(int amount)
    {
        if (amount > 0)
        {
            xp += amount;
            DebugLog.log("XP gained: +" + amount + " (Total: " + xp + "/" + MAX_XP + ")");
            
            // Handle level-ups with XP rollover
            while (xp >= MAX_XP)
            {
                xp -= MAX_XP;
                level++;
                DebugLog.log("LEVEL UP! Now level: " + level + " (XP: " + xp + "/" + MAX_XP + ")");
            }
        }
    }

    public void resetXP()
    {
        xp = 0;
    }

    public float getXPPercent()
    {
        return (float) xp / MAX_XP;
    }

    public int getLevel()
    {
        return level;
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
    
    // ========== Game Progression Methods ==========
    
    /**
     * Record a quiz result from the MainMapWorld Teacher NPC
     */
    public void recordMainMapNPCQuizResult(boolean correct)
    {
        if (mainMapNPCTotalCount < NPC_QUIZ_LIMIT)
        {
            mainMapNPCTotalCount++;
            if (correct)
            {
                mainMapNPCCorrectCount++;
            }
            
            // Unlock quests if 3 correct achieved
            if (!mainMapQuestsUnlocked && mainMapNPCCorrectCount >= CORRECT_NEEDED)
            {
                mainMapQuestsUnlocked = true;
            }
        }
    }
    
    /**
     * Check if MainMapWorld quests are unlocked (3/5 NPC quizzes correct)
     */
    public boolean areMainMapQuestsUnlocked()
    {
        return mainMapQuestsUnlocked;
    }
    
    /**
     * Check if MainMapWorld NPC has more quizzes available
     */
    public boolean hasMainMapNPCQuizzesRemaining()
    {
        return mainMapNPCTotalCount < NPC_QUIZ_LIMIT;
    }
    
    /**
     * Get progress on MainMapWorld NPC quizzes
     */
    public int getMainMapNPCProgress()
    {
        return mainMapNPCTotalCount;
    }
    
    public int getMainMapNPCCorrect()
    {
        return mainMapNPCCorrectCount;
    }
    
    /**
     * Record a quiz result from LabBiologyWorld NPC
     */
    public void recordLabBioNPCQuizResult(boolean correct)
    {
        labNPCTotalCountBio++;
        if (correct)
        {
            labNPCCorrectCountBio++;
        }
    }
    
    /**
     * Record a quiz result from LabFizicaWorld NPC
     */
    public void recordLabPhysNPCQuizResult(boolean correct)
    {
        labNPCTotalCountPhys++;
        if (correct)
        {
            labNPCCorrectCountPhys++;
        }
    }
    
    /**
     * Check if player is allowed to take quests in a lab
     * (Requires finishing MainMapWorld quizzes and progression)
     */
    public boolean canTakeLabQuests(LabType lab)
    {
        if (lab == LabType.BIOLOGY)
        {
            // Must have unlocked MainMapWorld quests first
            return mainMapQuestsUnlocked;
        }
        else if (lab == LabType.PHYSICS)
        {
            // Must have completed Biology first
            return completedLabs.contains(LabType.BIOLOGY) && mainMapQuestsUnlocked;
        }
        return false;
    }

    public int getLabBioQuizTotal()
    {
        return labNPCTotalCountBio;
    }

    public int getLabBioQuizCorrect()
    {
        return labNPCCorrectCountBio;
    }

    public boolean isLabBioQuizGateComplete()
    {
        return labNPCTotalCountBio >= NPC_QUIZ_LIMIT && labNPCCorrectCountBio >= CORRECT_NEEDED;
    }

    public int getLabPhysQuizTotal()
    {
        return labNPCTotalCountPhys;
    }

    public int getLabPhysQuizCorrect()
    {
        return labNPCCorrectCountPhys;
    }

    public boolean isLabPhysQuizGateComplete()
    {
        return labNPCTotalCountPhys >= NPC_QUIZ_LIMIT && labNPCCorrectCountPhys >= CORRECT_NEEDED;
    }

    /**
     * Record a quiz result from LabWorld NPC (Chemistry)
     */
    public void recordLabChemQuizResult(boolean correct)
    {
        labNPCTotalCountChem++;
        if (correct)
        {
            labNPCCorrectCountChem++;
        }
    }

    public int getLabChemQuizTotal()
    {
        return labNPCTotalCountChem;
    }

    public int getLabChemQuizCorrect()
    {
        return labNPCCorrectCountChem;
    }

    public boolean isLabChemQuizGateComplete()
    {
        return labNPCTotalCountChem >= NPC_QUIZ_LIMIT && labNPCCorrectCountChem >= CORRECT_NEEDED;
    }
    
    // MainMap mini-quest completion methods
    public boolean isRapidFireQuestComplete() { return rapidFireQuestComplete; }
    public void setRapidFireQuestComplete(boolean complete) { rapidFireQuestComplete = complete; }
    
    public boolean isKeySequenceQuestComplete() { return keySequenceQuestComplete; }
    public void setKeySequenceQuestComplete(boolean complete) { keySequenceQuestComplete = complete; }
    
    public boolean isAlternatingKeysQuestComplete() { return alternatingKeysQuestComplete; }
    public void setAlternatingKeysQuestComplete(boolean complete) { alternatingKeysQuestComplete = complete; }
    
    public boolean isDoubleTapSprintQuestComplete() { return doubleTapSprintQuestComplete; }
    public void setDoubleTapSprintQuestComplete(boolean complete) { doubleTapSprintQuestComplete = complete; }
    
    public boolean isComboChainQuestComplete() { return comboChainQuestComplete; }
    public void setComboChainQuestComplete(boolean complete) { comboChainQuestComplete = complete; }
    
    public boolean isDirectionDodgeQuestComplete() { return directionDodgeQuestComplete; }
    public void setDirectionDodgeQuestComplete(boolean complete) { directionDodgeQuestComplete = complete; }
}
