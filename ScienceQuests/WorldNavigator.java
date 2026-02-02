import greenfoot.Greenfoot;
import greenfoot.World;

public class WorldNavigator
{
    private WorldNavigator()
    {
    }

    public static void goToMainMap()
    {
        Greenfoot.setWorld(new MainMapWorld());
    }

    public static boolean tryEnterLab(LabType lab)
    {
        GameState state = GameState.getInstance();
        if (!state.canEnterLab(lab))
        {
            DebugLog.log("Access blocked to lab: " + lab);
            return false;
        }

        World target = null;
        if (lab == LabType.BIOLOGY)
        {
            target = new LabBiologyWorld();
        }
        else if (lab == LabType.PHYSICS)
        {
            target = new LabFizicaWorld();
        }
        else if (lab == LabType.CHEMISTRY)
        {
            target = new LabWorld();
        }

        if (target != null)
        {
            Greenfoot.setWorld(target);
            return true;
        }

        return false;
    }
}
