import greenfoot.*;

public class NPC extends Actor {

    private Quest quest;

    public NPC() {
        quest = new Quest("Adună 10 puncte de știință!");

        GreenfootImage img = new GreenfootImage(40, 40);
        img.setColor(Color.GREEN);
        img.fill();
        setImage(img);
    }

    public void talk() {
        World w = getWorld();

        // șterge dialogurile vechi
        w.removeObjects(w.getObjects(Dialog.class));

        // afișează dialog nou
        w.addObject(
            new Dialog("Profesor Atom: " + quest.getDescription()),
            w.getWidth() / 2,
            w.getHeight() - 50
        );
    }
}
