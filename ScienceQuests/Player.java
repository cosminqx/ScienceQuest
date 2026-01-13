import greenfoot.*;

public class Player extends Actor {

    private int speed = 4;
    private int xp = 0;

    public void act() {
        movePlayer();
        interact();
    }

    private void movePlayer() {
        if (Greenfoot.isKeyDown("w")) setLocation(getX(), getY() - speed);
        if (Greenfoot.isKeyDown("s")) setLocation(getX(), getY() + speed);
        if (Greenfoot.isKeyDown("a")) setLocation(getX() - speed, getY());
        if (Greenfoot.isKeyDown("d")) setLocation(getX() + speed, getY());
    }

    private void interact() {
        if (Greenfoot.isKeyDown("e")) {
            Actor npc = getOneIntersectingObject(NPC.class);
            if (npc != null) {
                ((NPC)npc).talk();
            }
        }
    }

    public void addXP(int value) {
        xp += value;
    }

    public int getXP() {
        return xp;
    }
}
