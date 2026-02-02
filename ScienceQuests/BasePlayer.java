import greenfoot.Actor;
import greenfoot.Greenfoot;
import greenfoot.World;

/**
 * BasePlayer - shared movement and collision logic for player characters.
 */
public abstract class BasePlayer extends Actor
{
    protected static final int DIR_UP = 0;
    protected static final int DIR_LEFT = 1;
    protected static final int DIR_DOWN = 2;
    protected static final int DIR_RIGHT = 3;
    protected static final int DIR_UP_LEFT = 4;
    protected static final int DIR_UP_RIGHT = 5;
    protected static final int DIR_DOWN_LEFT = 6;
    protected static final int DIR_DOWN_RIGHT = 7;

    protected int speed = 3;
    protected boolean isMoving = false;
    protected int currentDirection = DIR_DOWN;

    public void act()
    {
        if (!DialogueManager.getInstance().isDialogueActive())
        {
            handleMovement();
        }
        updateAnimation();
    }

    protected abstract int getHitboxWidth();

    protected abstract int getHitboxHeight();

    protected abstract int getHitboxOffsetY();

    protected abstract void onDirectionChanged(int newDirection);

    protected abstract void updateAnimation();

    private void handleMovement()
    {
        isMoving = false;

        boolean up = Greenfoot.isKeyDown("up");
        boolean down = Greenfoot.isKeyDown("down");
        boolean left = Greenfoot.isKeyDown("left");
        boolean right = Greenfoot.isKeyDown("right");

        if (up && down) { up = false; down = false; }
        if (left && right) { left = false; right = false; }

        int startX = getX();
        int startY = getY();
        int newX = startX;
        int newY = startY;
        int newDirection = currentDirection;

        if (up && left)
        {
            newY -= speed;
            newX -= speed;
            newDirection = DIR_UP_LEFT;
        }
        else if (up && right)
        {
            newY -= speed;
            newX += speed;
            newDirection = DIR_UP_RIGHT;
        }
        else if (down && left)
        {
            newY += speed;
            newX -= speed;
            newDirection = DIR_DOWN_LEFT;
        }
        else if (down && right)
        {
            newY += speed;
            newX += speed;
            newDirection = DIR_DOWN_RIGHT;
        }
        else if (up)
        {
            newY -= speed;
            newDirection = DIR_UP;
        }
        else if (down)
        {
            newY += speed;
            newDirection = DIR_DOWN;
        }
        else if (left)
        {
            newX -= speed;
            newDirection = DIR_LEFT;
        }
        else if (right)
        {
            newX += speed;
            newDirection = DIR_RIGHT;
        }

        if (newDirection != currentDirection)
        {
            currentDirection = newDirection;
            onDirectionChanged(newDirection);
        }

        World world = getWorld();
        if (world instanceof CollisionWorld)
        {
            CollisionWorld collisionWorld = (CollisionWorld) world;
            int hitboxWidth = getHitboxWidth();
            int hitboxHeight = getHitboxHeight();
            int hitboxOffsetY = getHitboxOffsetY();

            boolean fullMoveCollides = checkCollision(collisionWorld, newX, newY + hitboxOffsetY, hitboxWidth, hitboxHeight);
            if (fullMoveCollides)
            {
                // Try moving only horizontally
                boolean xMoveCollides = checkCollision(collisionWorld, newX, startY + hitboxOffsetY, hitboxWidth, hitboxHeight);
                if (!xMoveCollides && newX != startX)
                {
                    setLocation(newX, startY);
                    isMoving = true;
                    return;
                }

                // Try moving only vertically
                boolean yMoveCollides = checkCollision(collisionWorld, startX, newY + hitboxOffsetY, hitboxWidth, hitboxHeight);
                if (!yMoveCollides && newY != startY)
                {
                    setLocation(startX, newY);
                    isMoving = true;
                    return;
                }

                // Completely blocked
                isMoving = false;
                return;
            }
        }

        // No collision or non-collision world
        if (newX != startX || newY != startY)
        {
            setLocation(newX, newY);
            isMoving = up || down || left || right;
        }
        else
        {
            isMoving = false;
        }
    }

    private boolean checkCollision(CollisionWorld world, int screenX, int screenY, int width, int height)
    {
        int mapX = world.screenToMapX(screenX);
        int mapY = world.screenToMapY(screenY);
        return world.isCollisionAt(mapX, mapY, width, height);
    }
}
