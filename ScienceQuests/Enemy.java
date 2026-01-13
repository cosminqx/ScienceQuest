import greenfoot.*;

public class Enemy extends Actor {

    public void act() {
        move(1);
        if (isAtEdge()) turn(180);
    }
}
