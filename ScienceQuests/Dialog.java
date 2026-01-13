import greenfoot.*;

public class Dialog extends Actor {

    public Dialog(String text) {
        GreenfootImage img = new GreenfootImage(500, 80);
        img.setColor(Color.BLACK);
        img.fill();

        img.setColor(Color.WHITE);
        img.drawString(text, 20, 40);

        setImage(img);
    }
}
