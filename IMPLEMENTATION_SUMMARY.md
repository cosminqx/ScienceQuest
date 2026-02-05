# NPC Quiz Randomization & Game Progression Implementation

## Overview
Fixed the NPC quiz answer randomization bug and implemented a comprehensive game progression system where:
- MainMapWorld: NPC teacher gives 5 quizzes; 3 correct answers unlock mini-quests
- Biology Lab: Players answer NPC quizzes, then complete mini-quests
- Physics Lab: Same pattern repeats

## Changes Made

### 1. **DialogueQuestion.java** - Answer Randomization
**Problem:** The right answer was always at index 0

**Solution:**
- Added `shuffleAnswers()` method that uses Fisher-Yates shuffle
- Shuffles answer array after question creation
- Tracks and updates `correctAnswerIndex` to match the shuffled position
- Answers are now randomized every time a question is created

**Implementation:**
```java
private void shuffleAnswers() {
    if (answers.length <= 1) return;
    Random random = new Random();
    String correctAnswer = answers[correctAnswerIndex];
    
    // Fisher-Yates shuffle
    for (int i = answers.length - 1; i > 0; i--) {
        int j = random.nextInt(i + 1);
        String temp = answers[i];
        answers[i] = answers[j];
        answers[j] = temp;
    }
    
    // Find new index of correct answer
    for (int i = 0; i < answers.length; i++) {
        if (answers[i].equals(correctAnswer)) {
            correctAnswerIndex = i;
            break;
        }
    }
}
```

### 2. **GameState.java** - Progression Tracking
**Added Fields:**
```java
private int mainMapNPCCorrectCount = 0;        // Correct answers from MainMapWorld Teacher
private int mainMapNPCTotalCount = 0;          // Total questions asked
private static final int NPC_QUIZ_LIMIT = 5;   // Max 5 quizzes in MainMapWorld
private static final int CORRECT_NEEDED = 3;   // Need 3 correct to unlock quests
private boolean mainMapQuestsUnlocked = false; // Quest unlock flag
private int labNPCCorrectCountBio = 0;         // Biology Lab quiz tracking
private int labNPCTotalCountBio = 0;
private int labNPCCorrectCountPhys = 0;        // Physics Lab quiz tracking
private int labNPCTotalCountPhys = 0;
```

**Added Methods:**
- `recordMainMapNPCQuizResult(boolean correct)` - Records quiz attempt
- `areMainMapQuestsUnlocked()` - Checks if 3/5 correct achieved
- `hasMainMapNPCQuizzesRemaining()` - Checks if more quizzes available
- `getMainMapNPCProgress()` - Returns total attempts
- `getMainMapNPCCorrect()` - Returns correct count
- `recordLabBioNPCQuizResult(boolean correct)` - Records Biology Lab quiz
- `recordLabPhysNPCQuizResult(boolean correct)` - Records Physics Lab quiz
- `canTakeLabQuests(LabType lab)` - Checks progression requirements

### 3. **DialogueBox.java** - Enhanced Callbacks
**Added:**
- `onAnswerAttemptCallback` - Consumer that receives correctness boolean
- `setOnAnswerAttemptCallback(Consumer<Boolean>)` - Setter for callback
- Updated `confirmSelection()` to call callback with correctness flag

**Why:** Allows tracking both correct and incorrect answers in NPC quiz progression

### 4. **Teacher.java** - MainMapWorld NPC Progression
**Changes:**
- Limited to 5 quizzes total (NPC_QUIZ_LIMIT = 5)
- After 3 correct answers: quests unlock, debug message logged
- After all 5 quizzes: shows completion dialogue with progression message
- Uses `onAnswerAttemptCallback` to track results
- Updated `getDialogueText()` to show current progress (e.g., "Quiz 2 din 5 (1 corecte)")

**Key Logic:**
```java
public void recordMainMapNPCQuizResult(boolean correct) {
    if (mainMapNPCTotalCount < NPC_QUIZ_LIMIT) {
        mainMapNPCTotalCount++;
        if (correct) mainMapNPCCorrectCount++;
        
        // Auto-unlock at 3 correct
        if (!mainMapQuestsUnlocked && mainMapNPCCorrectCount >= CORRECT_NEEDED) {
            mainMapQuestsUnlocked = true;
        }
    }
}
```

### 5. **BiologyTeacher.java** - Lab NPC Enhancement
**Added:**
- `initiateNPCDialogue()` - Shows NPC quiz in Biology Lab
- Checks if lab is destroyed to determine interaction type
- If not destroyed: shows NPC quiz with progress tracking
- Uses `onAnswerAttemptCallback` to record lab-specific results
- Tracks results in `GameState.recordLabBioNPCQuizResult()`

**Interaction Logic:**
- If lab NOT destroyed: Show NPC quiz (initial progression)
- If lab IS destroyed: Show repair quiz (lab restoration)

## Game Flow Implemented

```
1. MainMapWorld
   ├─ Talk to Teacher (F key)
   ├─ Teacher gives Quiz 1-5
   ├─ After 3 correct → Quests UNLOCK
   ├─ Complete mini-quests (now accessible)
   └─ After all 5 quizzes → Message to go to Biology Lab
   
2. Biology Lab (after directed from MainMapWorld)
   ├─ Talk to BiologyTeacher (F key)
   ├─ BiologyTeacher gives NPC quiz(zes)
   ├─ Complete mini-quests
   ├─ Lab gets destroyed (triggers panic)
   ├─ Answer repair quiz (triggered automatically)
   └─ Lab repaired → Biology Lab complete
   
3. MainMapWorld (return)
   ├─ Unlock Physics Lab progression
   
4. Physics Lab (same as Biology Lab)
   └─ Repeat process
```

## How It Works

### NPC Quiz Progression in MainMapWorld:
1. Player starts in MainMapWorld
2. Presses F near Teacher
3. Teacher shows greeting + Quiz 1
4. Player answers (randomized options)
5. Result tracked: `GameState.recordMainMapNPCQuizResult(isCorrect)`
6. After 3 correct: `areMainMapQuestsUnlocked()` returns true
7. After 5 total: No more quizzes, show completion message

### Mini-Quest Access:
- Quests currently always accessible (can be restricted via `GameState.areMainMapQuestsUnlocked()` check in each quest)
- At 3/5 correct NPC answers, visibility/interactivity can be gated

### Lab Progression:
- Similar system in LabBiologyWorld with BiologyTeacher
- NPC quizzes give educational content before mini-quests
- Lab destruction and repair still work as before

## Files Modified

1. ✅ **DialogueQuestion.java** - Answer shuffling + imports
2. ✅ **GameState.java** - Progression tracking fields & methods
3. ✅ **DialogueBox.java** - Callback signature (Consumer<Boolean>)
4. ✅ **Teacher.java** - Limited quizzes, progression tracking
5. ✅ **BiologyTeacher.java** - NPC quiz dialogue support

## Testing Checklist

- [ ] Answers are randomized (not always first option)
- [ ] MainMapWorld Teacher gives exactly 5 quizzes
- [ ] Quests unlock after 3 correct answers
- [ ] After 5 quizzes, completion message shows
- [ ] BiologyTeacher shows NPC quiz when F pressed (before lab destroyed)
- [ ] Quiz results tracked correctly in GameState
- [ ] Progress displays "Quiz X din 5 (Y corecte)" correctly
- [ ] Progression flow: MainMap → BioLab → back to MainMap → PhysicsLab

## Known Limitations / Future Work

1. **Quest Unlock UI** - Quests are not visually gated yet. Can add:
   - Greyed-out quest markers until unlocked
   - Dialogue prompt "Need to answer 3 NPC quizzes first"
   - Quest actors can check `GameState.areMainMapQuestsUnlocked()`

2. **Physics Lab Setup** - Same pattern needs to be configured for PhysicsTeacher/LabFizicaWorld
   - Add NPC dialogue to PhysicsTeacher (similar to BiologyTeacher)
   - Update LabFizicaWorld to include PhysicsTeacher

3. **Visual Progression Indicators** - Could add:
   - Progress bar showing "3/5" quiz completion
   - DirectionArrow message update when progression changes
   - Animation when quests unlock

## Technical Details

### Answer Randomization Algorithm
- Uses Fisher-Yates shuffle for unbiased randomization
- Preserves correct answer semantic (tracks actual correct string)
- Re-indexes `correctAnswerIndex` after shuffle
- One Random instance per question ensures variety

### GameState Design
- Centralized progression state machine
- Separate tracking for each lab's NPC quizzes
- Methods to query unlock status at any time
- Follows singleton pattern (existing GameState design)

### Callback Flow
- DialogueBox.confirmSelection() → calls onAnswerAttemptCallback(isCorrect)
- Teacher receives callback, calls GameState.recordMainMapNPCQuizResult()
- isCorrect automatically unlocks quests if threshold reached

---

**Implementation Date:** February 5, 2026  
**Status:** Complete & Ready for Testing
