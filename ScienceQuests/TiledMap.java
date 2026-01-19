import greenfoot.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * TiledMap - lightweight TMX/TMJ loader for CSV-encoded tile layers.
 * Supports Floor, Objects, Objects1 layers with 48x48 tiles.
 * For TMJ: uses Collision object layer (rectangles) for collision detection.
 */
public class TiledMap
{
    public final int mapW;
    public final int mapH;
    public final int tileSize;
    private final int tilesetFirstGid;
    public final int[][] floor;
    public final int[][] objectsA;
    public final int[][] objectsB;
    public final boolean[][] solid;
    private final List<CollisionRect> collisionRects = new ArrayList<>();
    private GreenfootImage fullMapImage;
    private GreenfootImage tileset;
    private List<GreenfootImage> tileCache;

    public TiledMap(String tmxPath)
    {
        String content;
        try
        {
            content = new String(Files.readAllBytes(Paths.get(tmxPath)));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to read TMX: " + e.getMessage());
        }

        boolean isJson = tmxPath.endsWith(".tmj") || content.trim().startsWith("{");
        if (isJson)
        {
            mapW = extractJsonInt(content, "\"width\":");
            mapH = extractJsonInt(content, "\"height\":");
            tileSize = extractJsonInt(content, "\"tilewidth\":");
            tilesetFirstGid = extractTilesetFirstGid(content);

            floor = parseJsonTileLayer(content, "Floor", mapW, mapH);
            objectsA = parseJsonTileLayer(content, "Objects", mapW, mapH);
            objectsB = parseJsonTileLayer(content, "Objects1", mapW, mapH);
        }
        else
        {
            mapW = extractIntAttr(content, "width=\"", "\"");
            mapH = extractIntAttr(content, "height=\"", "\"");
            tileSize = extractIntAttr(content, "tilewidth=\"", "\"");
            tilesetFirstGid = extractTilesetFirstGid(content);

            floor = parseLayer(content, "Floor", mapW, mapH);
            objectsA = parseLayer(content, "Collision", mapW, mapH);
            objectsB = parseLayer(content, "Collision1", mapW, mapH);
        }

        loadTilesetImage();

        solid = new boolean[mapH][mapW];
        if (isJson)
        {
            buildSolidFromObjectLayer(content);
        }
        else
        {
            for (int y = 0; y < mapH; y++)
            {
                for (int x = 0; x < mapW; x++)
                {
                    solid[y][x] = (objectsA[y][x] != 0) || (objectsB[y][x] != 0);
                }
            }
        }

        logLayerCounts(isJson);

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
        if (layerPos == -1)
        {
            System.out.println("TMX layer not found: " + layerName + ", returning empty layer");
            return new int[h][w];
        }

        int dataStart = xml.indexOf("<data", layerPos);
        dataStart = xml.indexOf(">", dataStart) + 1;
        int dataEnd = xml.indexOf("</data>", dataStart);
        String dataBlock = xml.substring(dataStart, dataEnd).trim();

        String[] rows = dataBlock.split("\\n");
        int[][] layer = new int[h][w];
        int rowIndex = 0;
        try
        {
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
        }
        catch (Exception ex)
        {
            System.out.println("TMX parse error in layer: " + layerName + ": " + ex.getMessage());
        }
        return layer;
    }

    private void buildFullMapImage()
    {
        try
        {
            int widthPx = mapW * tileSize;
            int heightPx = mapH * tileSize;
            fullMapImage = new GreenfootImage(widthPx, heightPx);
            
            System.out.println("===== Building map image =====");
            System.out.println("Map dimensions: " + mapW + "x" + mapH + " tiles");
            System.out.println("Tile size: " + tileSize + "px");
            System.out.println("Full image: " + widthPx + "x" + heightPx + " pixels");
            System.out.println("Tileset loaded: " + (tileset != null));
            if (tileset != null)
            {
                System.out.println("Tileset size: " + tileset.getWidth() + "x" + tileset.getHeight());
                System.out.println("Tile cache size: " + tileCache.size());
            }
            System.out.println("Floor layer non-zero tiles: " + countNonZero(floor));
            System.out.println("Objects layer non-zero tiles: " + countNonZero(objectsA));
            System.out.println("Objects1 layer non-zero tiles: " + countNonZero(objectsB));
            System.out.println("Starting to draw layers...");

            // Draw visual layers in order: Floor -> Objects -> Objects1
            drawLayerOnto(fullMapImage, floor, "Floor");
            drawLayerOnto(fullMapImage, objectsA, "Objects");
            drawLayerOnto(fullMapImage, objectsB, "Objects1");
            
            System.out.println("===== Map building complete =====");
        }
        catch (Exception e)
        {
            System.out.println("ERROR building map image: " + e.getMessage());
            e.printStackTrace();
            // Create fallback empty image
            fullMapImage = new GreenfootImage(mapW * tileSize, mapH * tileSize);
            fullMapImage.setColor(new Color(0, 0, 0));
            fullMapImage.fillRect(0, 0, fullMapImage.getWidth(), fullMapImage.getHeight());
        }
    }

    private void drawLayerOnto(GreenfootImage target, int[][] layer, String layerName)
    {
        int drawn = 0;
        int failed = 0;
        int skipped = 0;
        int minGid = Integer.MAX_VALUE;
        int maxGid = 0;
        
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                int gid = layer[y][x];
                if (gid == 0) 
                { 
                    skipped++;
                    continue;
                }
                
                if (gid < minGid) minGid = gid;
                if (gid > maxGid) maxGid = gid;

                GreenfootImage tile = getTileFromGid(gid);
                if (tile == null)
                {
                    failed++;
                }
                else
                {
                    target.drawImage(tile, x * tileSize, y * tileSize);
                    drawn++;
                }
            }
        }
        System.out.println("  " + layerName + ": Drew " + drawn + " tiles (skipped " + skipped + ", failed " + failed + ")");
        if (minGid != Integer.MAX_VALUE)
        {
            System.out.println("    GID range: " + minGid + " - " + maxGid);
        }
    }
    
    private int countNonZero(int[][] layer)
    {
        int count = 0;
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                if (layer[y][x] != 0) count++;
            }
        }
        return count;
    }

    private void loadTilesetImage()
    {
        // Best guess: tileset image alongside images folder; fallback to floor.png style
        String[] candidatePaths = new String[] {
            "images/CoolSchool_tileset.png",
            "CoolSchool_tileset.png",
            "images/floor.png" // last resort
        };

        System.out.println("=== Tileset Loading ===");
        for (String path : candidatePaths)
        {
            try
            {
                tileset = new GreenfootImage(path);
                System.out.println("✓ Tileset loaded from: " + path);
                break;
            }
            catch (Exception e)
            {
                System.out.println("✗ Failed to load: " + path);
                tileset = null;
            }
        }

        tileCache = new java.util.ArrayList<>();
        if (tileset != null)
        {
            int cols = tileset.getWidth() / tileSize;
            int rows = tileset.getHeight() / tileSize;
            int total = cols * rows;
            System.out.println("✓ Tileset dimensions: " + cols + " cols x " + rows + " rows = " + total + " tiles");
            for (int i = 0; i < total; i++)
            {
                int sx = (i % cols) * tileSize;
                int sy = (i / cols) * tileSize;
                GreenfootImage tile = new GreenfootImage(tileSize, tileSize);
                tile.drawImage(tileset, -sx, -sy);
                tileCache.add(tile);
            }
        }
        else
        {
            System.out.println("✗ WARNING: Tileset not found! Using fallback colors.");
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

    private int extractTilesetFirstGid(String xmlOrJson)
    {
        if (xmlOrJson.contains("<tileset "))
        {
            int tsPos = xmlOrJson.indexOf("<tileset ");
            if (tsPos == -1) return 1; // fallback
            int value = extractIntAttr(xmlOrJson.substring(tsPos), "firstgid=\"", "\"");
            return value > 0 ? value : 1;
        }
        int pos = xmlOrJson.indexOf("\"firstgid\":");
        if (pos == -1) return 1;
        int value = extractJsonInt(xmlOrJson.substring(pos), "\"firstgid\":");
        return value > 0 ? value : 1;
    }

    private void logLayerCounts(boolean isJson)
    {
        if (isJson)
        {
            System.out.println("TMJ layers: Floor non-zero=" + countNonZero(floor) +
                               " Objects=" + countNonZero(objectsA) +
                               " Objects1=" + countNonZero(objectsB));
        }
        else
        {
            System.out.println("TMX layers: Floor non-zero=" + countNonZero(floor) +
                               " Collision=" + countNonZero(objectsA) +
                               " Collision1=" + countNonZero(objectsB));
        }
    }

    public GreenfootImage getFullMapImage()
    {
        return new GreenfootImage(fullMapImage);
    }

    private int extractJsonInt(String json, String key)
    {
        int pos = json.indexOf(key);
        if (pos == -1) return 0;
        int start = pos + key.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)))) end++;
        try { return Integer.parseInt(json.substring(start, end)); } catch (Exception e) { return 0; }
    }

    private int[][] parseJsonTileLayer(String json, String layerName, int w, int h)
    {
        // Find the layer by name
        int namePos = json.indexOf("\"name\":\"" + layerName + "\"");
        if (namePos == -1)
        {
            System.out.println("TMJ layer not found: " + layerName + ", returning empty layer");
            return new int[h][w];
        }
        
        // In TMJ, "data" comes BEFORE "name" in each layer object, so search backwards
        int searchStart = Math.max(0, namePos - 2000); // Search back up to 2000 chars
        int dataStart = json.lastIndexOf("\"data\":[", namePos);
        if (dataStart == -1 || dataStart < searchStart)
        {
            System.out.println("TMJ data array not found in layer: " + layerName);
            return new int[h][w];
        }
        dataStart += 8; // Skip past "data":[
        
        // Find the end of the data array by looking for ],
        int dataEnd = json.indexOf("],", dataStart);
        if (dataEnd == -1)
        {
            System.out.println("TMJ data array end not found in layer: " + layerName);
            return new int[h][w];
        }
        
        String data = json.substring(dataStart, dataEnd);
        String[] ids = data.split(",");
        int[][] layer = new int[h][w];
        int idx = 0;
        try
        {
            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    if (idx >= ids.length) { layer[y][x] = 0; }
                    else
                    {
                        int raw = Integer.parseInt(ids[idx].trim());
                        int masked = raw & 0x1FFFFFFF; // strip flip flags
                        layer[y][x] = masked;
                    }
                    idx++;
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("TMJ parse error in layer: " + layerName + ": " + ex.getMessage());
        }
        return layer;
    }

    private void buildSolidFromObjectLayer(String json)
    {
        int layerPos = json.indexOf("\"name\":\"Collision\"");
        if (layerPos == -1) {
            System.out.println("No Collision object layer found in TMJ");
            return;
        }
        int objsStart = json.indexOf("\"objects\":[", layerPos);
        if (objsStart == -1) return;
        int objsEnd = json.indexOf(']', objsStart);
        String objs = json.substring(objsStart + 10, objsEnd);
        String[] entries = objs.split("\\},\\s*\\{");
        
        System.out.println("Parsing Collision object layer with " + entries.length + " objects...");
        
        for (String e : entries)
        {
            String s = e.replace("{", "").replace("}", "");
            double x = extractJsonDouble(s, "\"x\":");
            double y = extractJsonDouble(s, "\"y\":");
            double w = extractJsonDouble(s, "\"width\":");
            double h = extractJsonDouble(s, "\"height\":");
            if (w <= 0 || h <= 0) continue;
            
            // Store collision rectangle in map pixel coordinates
            int rectX = (int)Math.round(x);
            int rectY = (int)Math.round(y);
            int rectW = (int)Math.round(w);
            int rectH = (int)Math.round(h);
            collisionRects.add(new CollisionRect(rectX, rectY, rectW, rectH));
            
            System.out.println("  Collision rect " + collisionRects.size() + ": x=" + rectX + " y=" + rectY + 
                               " w=" + rectW + " h=" + rectH);
            
            // Also mark solid grid for reference
            int tx0 = (int)Math.floor(x / tileSize);
            int ty0 = (int)Math.floor(y / tileSize);
            int tx1 = (int)Math.floor((x + w - 1) / tileSize);
            int ty1 = (int)Math.floor((y + h - 1) / tileSize);
            for (int ty = Math.max(0, ty0); ty <= Math.min(mapH - 1, ty1); ty++)
            {
                for (int tx = Math.max(0, tx0); tx <= Math.min(mapW - 1, tx1); tx++)
                {
                    solid[ty][tx] = true;
                }
            }
        }
        System.out.println("Loaded " + collisionRects.size() + " collision rectangles from Collision layer");
    }

    private double extractJsonDouble(String json, String key)
    {
        int pos = json.indexOf(key);
        if (pos == -1) return 0.0;
        int start = pos + key.length();
        while (start < json.length() && (Character.isWhitespace(json.charAt(start)) || json.charAt(start)==':')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end)=='.')) end++;
        try { return Double.parseDouble(json.substring(start, end)); } catch (Exception ex) { return 0.0; }
    }

    public List<CollisionRect> getCollisionRects()
    {
        return collisionRects;
    }
    
    public static class CollisionRect
    {
        public final int x, y, w, h;
        public CollisionRect(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}
