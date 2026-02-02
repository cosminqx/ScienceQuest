import greenfoot.*;

/**
 * QuestBlock - A blocking object that requires completing a timing mini-quest to remove
 */
public class QuestBlock extends Actor
{
    private boolean isActive = true;
    private boolean questStarted = false;
    private TimingQuestUI questUI;
    private int mapX;
    private int mapY;
    
    public QuestBlock(int mapX, int mapY)
    {
        this.mapX = mapX;
        this.mapY = mapY;
        
        // Create a visible block image
        GreenfootImage image = new GreenfootImage(48, 48);
        image.setColor(new Color(139, 69, 19)); // Brown color for a wooden crate
        image.fillRect(0, 0, 48, 48);
        image.setColor(new Color(101, 50, 13)); // Darker brown for border
        image.drawRect(0, 0, 47, 47);
        image.drawRect(2, 2, 43, 43);
        
        // Draw X pattern
        image.setColor(new Color(160, 82, 45));
        image.drawLine(10, 10, 37, 37);
        image.drawLine(37, 10, 10, 37);
        
        setImage(image);
    }
    
    public void act()
    {
        if (!isActive) return;
        
        // Check if player is nearby and presses space
        World world = getWorld();
        if (world == null) return;
        
        Actor player = null;
        if (!world.getObjects(Boy.class).isEmpty())
        {
            player = world.getObjects(Boy.class).get(0);
        }
        else if (!world.getObjects(Girl.class).isEmpty())
        {
            player = world.getObjects(Girl.class).get(0);
        }
        
        if (player != null)
        {
            // Calculate distance to player
            int dx = Math.abs(player.getX() - getX());
            int dy = Math.abs(player.getY() - getY());
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // If player is close and presses space, start the quest
            if (distance < 100 && !questStarted)
            {
                if (Greenfoot.isKeyDown("space"))
                {
                    startQuest();
                }
            }
        }
        
        // Update quest UI if active
        if (questStarted && questUI != null)
        {
            if (questUI.isCompleted())
            {
                completeQuest();
            }
            else if (questUI.isFailed())
            {
                questUI.reset();
            }
        }
    }
    
    private void startQuest()
    {
        questStarted = true;
        questUI = new TimingQuestUI(this);
        getWorld().addObject(questUI, getWorld().getWidth() / 2, getWorld().getHeight() / 2);
        System.out.println("Quest started! Press SPACE when the indicator is in the green zone!");
    }
    
    private void completeQuest()
    {
        isActive = false;
        
        // Remove the quest UI
        if (questUI != null && questUI.getWorld() != null)
        {
            getWorld().removeObject(questUI);
        }
        
        // Remove this block from collision
        if (getWorld() instanceof LabWorld)
        {
            LabWorld labWorld = (LabWorld) getWorld();
            labWorld.removeQuestBlockCollision(mapX, mapY);
        }
        
        // Remove this actor
        getWorld().removeObject(this);
        
        System.out.println("Quest completed! Block removed.");
    }
    
    public boolean isActive()
    {
        return isActive;
    }
    
    public int getMapX()
    {
        return mapX;
    }
    
    public int getMapY()
    {
        return mapY;
    }
}
