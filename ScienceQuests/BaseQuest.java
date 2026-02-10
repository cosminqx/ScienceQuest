import greenfoot.*;

/**
 * BaseQuest - Shared behavior for mini-quests (overlay, start gating, floating marker).
 */
public abstract class BaseQuest extends Actor
{
    protected final int mapX;
    protected final int mapY;

    protected boolean questActive = false;
    protected boolean completed = false;
    protected int interactionCooldown = 0;

    protected OverlayLayer overlay = null;
    protected int resultDisplayTicks = 0;

    protected int baseY = 0;
    protected boolean baseYSet = false;
    protected int floatTick = 0;

    protected boolean startKeyDown = false;
    protected boolean tutorialActive = false;

    protected BaseQuest(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
    }

    protected Actor getPlayer()
    {
        World world = getWorld();
        if (world == null) return null;

        java.util.List<Boy> boys = world.getObjects(Boy.class);
        if (!boys.isEmpty()) return boys.get(0);

        java.util.List<Girl> girls = world.getObjects(Girl.class);
        if (!girls.isEmpty()) return girls.get(0);

        return null;
    }

    protected boolean isPlayerInRange(Actor player, int range)
    {
        int dx = player.getX() - getX();
        int dy = player.getY() - getY();
        return Math.sqrt(dx * dx + dy * dy) < range;
    }

    protected boolean canStartQuest(Actor player, int range)
    {
        if (player == null) return false;
        if (!isPlayerInRange(player, range)) return false;
        if (DialogueManager.getInstance().isDialogueActive()) return false;
        if (GameState.getInstance().isMiniQuestActive()) return false;
        return true;
    }

    protected void beginQuest()
    {
        questActive = true;
        GameState.getInstance().setMiniQuestActive(true);
    }

    protected void endQuest()
    {
        questActive = false;
        GameState.getInstance().setMiniQuestActive(false);
    }

    protected void initBasePosition()
    {
        if (!baseYSet && getWorld() != null)
        {
            baseY = getY();
            baseYSet = true;
        }
    }

    protected void updateFloating()
    {
        if (!baseYSet) return;
        floatTick++;
        int offset = (int) (Math.sin(floatTick * 0.12) * 4);
        setLocation(getX(), baseY + offset);
    }

    protected void ensureOverlay()
    {
        World world = getWorld();
        if (world == null) return;
        if (overlay == null || overlay.getWorld() == null)
        {
            overlay = new OverlayLayer();
            world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        }
    }

    protected void clearOverlay()
    {
        if (overlay != null && overlay.getWorld() != null)
        {
            getWorld().removeObject(overlay);
            overlay = null;
        }
    }

    protected void updateResultOverlayTicks()
    {
        if (completed && resultDisplayTicks > 0)
        {
            resultDisplayTicks--;
            if (resultDisplayTicks == 0)
            {
                clearOverlay();
            }
        }
    }

    public int getMapX()
    {
        return mapX;
    }

    public int getMapY()
    {
        return mapY;
    }

    public boolean isCompleted()
    {
        return completed;
    }
}
