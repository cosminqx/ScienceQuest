import greenfoot.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * TiledMap - lightweight TMX loader for CSV-encoded tile layers.
 * Supports Floor, Collision, and Collision1 layers with 48x48 tiles.
 */
public class TiledMap
{
    public final int mapW;
    public final int mapH;
    public final int tileSize;
    private final int tilesetFirstGid;
    public final int[][] floor;
    public final int[][] collisionA;
    public final int[][] collisionB;
    public final boolean[][] solid;
    private GreenfootImage fullMapImage;
    private GreenfootImage tileset;
    private java.util.List<GreenfootImage> tileCache;

    public TiledMap(String tmxPath)
    {
        String xml;
        try
        {
            xml = new String(Files.readAllBytes(Paths.get(tmxPath)));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read TMX: " + e.getMessage());
        }

        mapW = extractIntAttr(xml, "width=\"", "\"");
        mapH = extractIntAttr(xml, "height=\"", "\"");
        tileSize = extractIntAttr(xml, "tilewidth=\"", "\"");
        tilesetFirstGid = extractTilesetFirstGid(xml);

        floor = parseLayer(xml, "Floor", mapW, mapH);
        collisionA = parseLayer(xml, "Collision", mapW, mapH);
        collisionB = parseLayer(xml, "Collision1", mapW, mapH);

        loadTilesetImage();

        solid = new boolean[mapH][mapW];
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                solid[y][x] = (collisionA[y][x] != 0) || (collisionB[y][x] != 0);
            }
        }

        logLayerCounts();

        buildFullMapImage();
    }

    private int extractIntAttr(String xml, String prefix, String terminator)
    {
        int start = xml.indexOf(prefix);
        if (start == -1) throw new RuntimeException("Missing attribute: " + prefix);
        start += prefix.length();
        int end = xml.indexOf(terminator, start);
        return Integer.parseInt(xml.substring(start, end));
    }

    private int[][] parseLayer(String xml, String layerName, int w, int h)
    {
        int layerPos = xml.indexOf("name=\"" + layerName + "\"");
        if (layerPos == -1) throw new RuntimeException("Layer not found: " + layerName);

        int dataStart = xml.indexOf("<data", layerPos);
        dataStart = xml.indexOf(">", dataStart) + 1;
        int dataEnd = xml.indexOf("</data>", dataStart);
        String dataBlock = xml.substring(dataStart, dataEnd).trim();

        String[] rows = dataBlock.split("\\n");
        int[][] layer = new int[h][w];
        int rowIndex = 0;
        for (String row : rows)
        {
            String trimmed = row.trim();
            if (trimmed.isEmpty()) continue;
            String[] ids = trimmed.split(",");
            for (int x = 0; x < w && x < ids.length; x++)
            {
                int raw = Integer.parseInt(ids[x].trim());
                int masked = raw & 0x1FFFFFFF; // strip Tiled flip/rotation flags
                layer[rowIndex][x] = masked;
            }
            rowIndex++;
            if (rowIndex >= h) break;
        }
        return layer;
    }

    private void buildFullMapImage()
    {
        int widthPx = mapW * tileSize;
        int heightPx = mapH * tileSize;
        fullMapImage = new GreenfootImage(widthPx, heightPx);

        // Draw visual layers in order: Floor -> Collision -> Collision1 (if they contain art)
        drawLayerOnto(fullMapImage, floor);
        drawLayerOnto(fullMapImage, collisionA);
        drawLayerOnto(fullMapImage, collisionB);
    }

    private void drawLayerOnto(GreenfootImage target, int[][] layer)
    {
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                int gid = layer[y][x];
                if (gid == 0) continue;

                GreenfootImage tile = getTileFromGid(gid);
                target.drawImage(tile, x * tileSize, y * tileSize);
            }
        }
    }

    private void loadTilesetImage()
    {
        // Best guess: tileset image alongside images folder; fallback to floor.png style
        String[] candidatePaths = new String[] {
            "images/CoolSchool_tileset.png",
            "CoolSchool_tileset.png",
            "images/floor.png" // last resort
        };

        for (String path : candidatePaths)
        {
            try
            {
                tileset = new GreenfootImage(path);
                break;
            }
            catch (Exception e)
            {
                tileset = null;
            }
        }

        tileCache = new java.util.ArrayList<>();
        if (tileset != null)
        {
            int cols = tileset.getWidth() / tileSize;
            int rows = tileset.getHeight() / tileSize;
            int total = cols * rows;
            for (int i = 0; i < total; i++)
            {
                int sx = (i % cols) * tileSize;
                int sy = (i / cols) * tileSize;
                GreenfootImage tile = new GreenfootImage(tileSize, tileSize);
                tile.drawImage(tileset, -sx, -sy);
                tileCache.add(tile);
            }
        }
    }

    private GreenfootImage getTileFromGid(int gid)
    {
        int index = gid - tilesetFirstGid;

        if (tileset != null && index >= 0 && index < tileCache.size())
        {
            return new GreenfootImage(tileCache.get(index)); // return a copy to avoid mutation
        }

        // Fallback colored tile if tileset missing or index out of range
        GreenfootImage fallback = new GreenfootImage(tileSize, tileSize);
        fallback.setColor(new Color(90, 140, 90));
        fallback.fillRect(0, 0, tileSize, tileSize);
        return fallback;
    }

    private int extractTilesetFirstGid(String xml)
    {
        int tsPos = xml.indexOf("<tileset ");
        if (tsPos == -1) return 1; // fallback
        int value = extractIntAttr(xml.substring(tsPos), "firstgid=\"", "\"");
        return value > 0 ? value : 1;
    }

    private void logLayerCounts()
    {
        System.out.println("TMX layers: Floor non-zero=" + countNonZero(floor) +
                           " Collision=" + countNonZero(collisionA) +
                           " Collision1=" + countNonZero(collisionB));
    }

    private int countNonZero(int[][] layer)
    {
        int c = 0;
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                if (layer[y][x] != 0) c++;
            }
        }
        return c;
    }

    public GreenfootImage getFullMapImage()
    {
        return new GreenfootImage(fullMapImage);
    }
}
