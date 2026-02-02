public interface CollisionWorld
{
    int screenToMapX(int screenX);

    int screenToMapY(int screenY);

    boolean isCollisionAt(int mapX, int mapY, int width, int height);
}
