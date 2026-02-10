import greenfoot.*;

/**
 * QuizNPCBase - Shared proximity and interaction handling for quiz NPCs.
 */
public abstract class QuizNPCBase extends Actor implements NPC
{
    protected static final int INTERACTION_DISTANCE = 80;

    protected TeacherInteractionDisplay interactionDisplay = new TeacherInteractionDisplay();
    protected boolean interactionArmed = false;
    protected int dialogueCooldown = 0;

    @Override
    public void act()
    {
        if (dialogueCooldown > 0)
        {
            dialogueCooldown--;
        }

        World world = getWorld();
        if (world == null) return;

        onWorldTick(world);

        Actor player = getPlayer(world);
        if (player == null)
        {
            removePrompt(world);
            return;
        }

        double distance = getDistance(player, this);
        boolean inRange = distance < INTERACTION_DISTANCE;
        updatePrompt(world, inRange);

        if (!isInteractionEnabled())
        {
            resetArming();
            return;
        }

        if (GameState.getInstance().isMiniQuestActive())
        {
            resetArming();
            return;
        }

        if (!inRange)
        {
            resetArming();
            return;
        }

        DialogueManager manager = DialogueManager.getInstance();
        if (manager.isDialogueActive())
        {
            return;
        }

        if (interactionArmed)
        {
            interactionArmed = false;
            dialogueCooldown = 15;
            return;
        }

        if (dialogueCooldown == 0)
        {
            interactionArmed = true;
            onInteract(world);
        }
    }

    protected void onWorldTick(World world)
    {
        // Optional hook for subclasses.
    }

    protected abstract boolean isInteractionEnabled();

    protected abstract void onInteract(World world);

    protected boolean shouldShowPrompt()
    {
        return true;
    }

    protected void updatePrompt(World world, boolean inRange)
    {
        if (!shouldShowPrompt())
        {
            removePrompt(world);
            return;
        }

        if (inRange)
        {
            if (interactionDisplay.getWorld() == null)
            {
                world.addObject(interactionDisplay, getX(), getY() - 60);
            }
            else
            {
                interactionDisplay.setLocation(getX(), getY() - 60);
            }
        }
        else
        {
            removePrompt(world);
        }
    }

    protected void removePrompt(World world)
    {
        if (interactionDisplay.getWorld() != null)
        {
            world.removeObject(interactionDisplay);
        }
    }

    protected void resetArming()
    {
        interactionArmed = false;
        dialogueCooldown = 0;
    }

    protected Actor getPlayer(World world)
    {
        if (!world.getObjects(Boy.class).isEmpty())
        {
            return world.getObjects(Boy.class).get(0);
        }
        if (!world.getObjects(Girl.class).isEmpty())
        {
            return world.getObjects(Girl.class).get(0);
        }
        return null;
    }

    protected double getDistance(Actor a, Actor b)
    {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
