import greenfoot.*;

public class ScienceWorld extends World {

    public ScienceWorld() {
        super(800, 600, 1);

        Player player = new Player();
        addObject(player, 400, 300);

        NPC scientist = new NPC();
        addObject(scientist, 200, 300);

        Enemy virus = new Enemy();
        addObject(virus, 600, 300);

        addObject(new HUD(player), 100, 30);
    }
}
