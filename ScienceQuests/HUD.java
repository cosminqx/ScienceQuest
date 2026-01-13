import greenfoot.*;

public class HUD extends Actor {

    private Player player;

    public HUD(Player player) {
        this.player = player;
        updateImage();
    }

    public void act() {
        updateImage();
    }

    private void updateImage() {
        GreenfootImage img = new GreenfootImage(200, 40);
        img.setColor(Color.WHITE);
        img.drawString("XP: " + player.getXP(), 10, 25);
        setImage(img);
    }
}
