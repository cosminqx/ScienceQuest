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
    private final List<int[][]> tileLayers = new ArrayList<>();
    private final List<String> tileLayerNames = new ArrayList<>();
    private final List<Float> tileLayerOpacity = new ArrayList<>();
    public final boolean[][] solid;
    public final List<CollisionRect> collisionRects = new ArrayList<>();
    private GreenfootImage fullMapImage;
    private GreenfootImage tileset;
    private List<GreenfootImage> tileCache;
    private List<TilesetInfo> tilesets = new ArrayList<>();
    
    private static class TilesetInfo {
        int firstgid;
        List<GreenfootImage> tiles;
        TilesetInfo(int gid, List<GreenfootImage> t) { firstgid = gid; tiles = t; }
    }

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

        boolean isJson = tmxPath.endsWith(".tmj") || tmxPath.endsWith(".json") || content.trim().startsWith("{");
        if (isJson)
        {
            mapW = extractJsonInt(content, "\"width\":");
            mapH = extractJsonInt(content, "\"height\":");
            tileSize = extractJsonInt(content, "\"tilewidth\":");
            tilesetFirstGid = extractTilesetFirstGid(content);

            // Parse every tile layer in order (for multi-layer maps like lab_noapte_2.json)
            parseAllJsonTileLayers(content, mapW, mapH);

            // Keep backward compatibility fields for existing code paths
            floor = tileLayers.size() > 0 ? tileLayers.get(0) : new int[mapH][mapW];
            objectsA = tileLayers.size() > 1 ? tileLayers.get(1) : new int[mapH][mapW];
            objectsB = tileLayers.size() > 2 ? tileLayers.get(2) : new int[mapH][mapW];
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

            // Populate tileLayers list for unified rendering path
            tileLayers.add(floor);
            tileLayerNames.add("Floor");
            tileLayers.add(objectsA);
            tileLayerNames.add("Collision");
            tileLayers.add(objectsB);
            tileLayerNames.add("Collision1");
        }

        loadTilesetImage();
        if (isJson) {
            loadMultipleTilesets(content);
        }

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
            DebugLog.log("TMX layer not found: " + layerName + ", returning empty layer");
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
                    // Keep the raw value with flip flags - they'll be processed during rendering
                    layer[rowIndex][x] = raw;
                }
                rowIndex++;
                if (rowIndex >= h) break;
            }
        }
        catch (Exception ex)
        {
            DebugLog.log("TMX parse error in layer: " + layerName + ": " + ex.getMessage());
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
            
            DebugLog.log("===== Building map image =====");
            DebugLog.log("Map dimensions: " + mapW + "x" + mapH + " tiles");
            DebugLog.log("Tile size: " + tileSize + "px");
            DebugLog.log("Full image: " + widthPx + "x" + heightPx + " pixels");
            DebugLog.log("Tileset loaded: " + (tileset != null));
            if (tileset != null)
            {
                DebugLog.log("Tileset size: " + tileset.getWidth() + "x" + tileset.getHeight());
                DebugLog.log("Tile cache size: " + tileCache.size());
            }
            DebugLog.log("Layers parsed: " + tileLayers.size());
            for (int i = 0; i < tileLayers.size(); i++)
            {
                int nonZero = countNonZero(tileLayers.get(i));
                DebugLog.log("  Layer " + i + " (" + tileLayerNames.get(i) + "): non-zero tiles=" + nonZero);
            }
            DebugLog.log("Starting to draw layers in order...");

            // Draw every tile layer in the order they appear in TMJ
            for (int i = 0; i < tileLayers.size(); i++)
            {
                String name = i < tileLayerNames.size() ? tileLayerNames.get(i) : ("Layer " + i);
                float opacity = i < tileLayerOpacity.size() ? tileLayerOpacity.get(i) : 1.0f;
                drawLayerOnto(fullMapImage, tileLayers.get(i), name, opacity);
            }
            
            DebugLog.log("===== Map building complete =====");
        }
        catch (Exception e)
        {
            DebugLog.log("ERROR building map image: " + e.getMessage());
            e.printStackTrace();
            // Create fallback empty image
            fullMapImage = new GreenfootImage(mapW * tileSize, mapH * tileSize);
            fullMapImage.setColor(new Color(0, 0, 0));
            fullMapImage.fillRect(0, 0, fullMapImage.getWidth(), fullMapImage.getHeight());
        }
    }

    private void drawLayerOnto(GreenfootImage target, int[][] layer, String layerName, float opacity)
    {
        int drawn = 0;
        int failed = 0;
        int skipped = 0;
        int minGid = Integer.MAX_VALUE;
        int maxGid = 0;
        int failedCount = 0;
        int maxFailsToReport = 5; // Only report first 5 failures per layer
        
        for (int y = 0; y < mapH; y++)
        {
            for (int x = 0; x < mapW; x++)
            {
                int rawGid = layer[y][x];
                if (rawGid == 0) 
                { 
                    skipped++;
                    continue;
                }
                
                // Extract flip flags and actual GID
                boolean flipH = (rawGid & 0x80000000) != 0;
                boolean flipV = (rawGid & 0x40000000) != 0;
                boolean flipD = (rawGid & 0x20000000) != 0;
                int gid = rawGid & 0x1FFFFFFF; // Remove flip flags
                
                if (gid < minGid) minGid = gid;
                if (gid > maxGid) maxGid = gid;

                GreenfootImage tile = getTileFromGid(gid);
                if (tile == null)
                {
                    failed++;
                    if (failedCount < maxFailsToReport) {
                        DebugLog.log("    FAILED: GID " + gid + " at (" + x + ", " + y + ")");
                        failedCount++;
                    }
                }
                else
                {
                    // Apply transformations if needed
                    if (flipH || flipV || flipD)
                    {
                        tile = applyTileTransform(tile, flipH, flipV, flipD);
                    }
                    
                    // Apply opacity if not fully opaque
                    if (opacity < 1.0f)
                    {
                        tile = applyOpacity(tile, opacity);
                    }
                    
                    target.drawImage(tile, x * tileSize, y * tileSize);
                    drawn++;
                }
            }
        }
        DebugLog.log("  " + layerName + ": Drew " + drawn + " tiles (skipped " + skipped + ", failed " + failed + ")");
        if (minGid != Integer.MAX_VALUE)
        {
            DebugLog.log("    GID range: " + minGid + " - " + maxGid);
        }
        
        // Report available tilesets
        if (!tilesets.isEmpty()) {
            System.out.print("    Available tilesets: ");
            for (int i = 0; i < tilesets.size(); i++) {
                System.out.print("gid=" + tilesets.get(i).firstgid + 
                    " (tiles=" + tilesets.get(i).tiles.size() + ") ");
            }
            DebugLog.log("");
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
            "images/48px/tilesFloor.png",
            "images/48px/tilesWalls.png",
            "images/48px/tilesStuff.png",
            "CoolSchool_tileset.png",
            "images/floor.png" // last resort
        };

        DebugLog.log("=== Tileset Loading ===");
        for (String path : candidatePaths)
        {
            try
            {
                tileset = new GreenfootImage(path);
                DebugLog.log("✓ Tileset loaded from: " + path);
                break;
            }
            catch (Exception e)
            {
                DebugLog.log("✗ Failed to load: " + path);
                tileset = null;
            }
        }

        tileCache = new java.util.ArrayList<>();
        if (tileset != null)
        {
            int cols = tileset.getWidth() / tileSize;
            int rows = tileset.getHeight() / tileSize;
            int total = cols * rows;
            DebugLog.log("✓ Tileset dimensions: " + cols + " cols x " + rows + " rows = " + total + " tiles");
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
            DebugLog.log("✗ WARNING: Tileset not found! Using fallback colors.");
        }
    }
    
    private void loadMultipleTilesets(String json) {
        DebugLog.log("=== Loading Multiple Tilesets ===");
        int tsPos = json.indexOf("\"tilesets\":[");
        if (tsPos == -1) return;
        
        int endPos = json.indexOf("]", tsPos);
        String tilesetsBlock = json.substring(tsPos, endPos);
        
        // Parse each tileset entry
        int searchFrom = 0;
        while (true) {
            int nextTileset = tilesetsBlock.indexOf("\"firstgid\":", searchFrom);
            if (nextTileset == -1) break;
            
            int gid = extractJsonInt(tilesetsBlock.substring(nextTileset), "\"firstgid\":");
            int sourcePos = tilesetsBlock.indexOf("\"source\":", nextTileset);
            if (sourcePos == -1 || sourcePos > nextTileset + 200) {
                searchFrom = nextTileset + 10;
                continue;
            }
            
            String source = extractJsonString(tilesetsBlock.substring(sourcePos), "\"source\":");
            if (source.isEmpty()) {
                searchFrom = nextTileset + 10;
                continue;
            }
            
            // Extract filename from source path
            String filename = source.replace("\\/", "/");
            if (filename.contains("/")) {
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            }
            filename = filename.replace(".tsx", ".png");
            
            // Try to load the tileset image
            String[] paths = new String[] {
                "images/48px/" + filename,
                "images/" + filename,
                filename
            };
            
            GreenfootImage tilesetImg = null;
            for (String path : paths) {
                try {
                    tilesetImg = new GreenfootImage(path);
                    DebugLog.log("✓ Loaded tileset gid=" + gid + " from: " + path);
                    break;
                } catch (Exception e) {
                    // Try next path
                }
            }
            
            if (tilesetImg != null) {
                List<GreenfootImage> tiles = new ArrayList<>();
                int cols = tilesetImg.getWidth() / tileSize;
                int rows = tilesetImg.getHeight() / tileSize;
                int total = cols * rows;
                
                for (int i = 0; i < total; i++) {
                    int sx = (i % cols) * tileSize;
                    int sy = (i / cols) * tileSize;
                    GreenfootImage tile = new GreenfootImage(tileSize, tileSize);
                    tile.drawImage(tilesetImg, -sx, -sy);
                    tiles.add(tile);
                }
                
                tilesets.add(new TilesetInfo(gid, tiles));
                DebugLog.log("  -> " + total + " tiles loaded");
            } else {
                DebugLog.log("✗ Failed to load tileset: " + filename);
            }
            
            searchFrom = nextTileset + 10;
        }
        
        DebugLog.log("Total tilesets loaded: " + tilesets.size());
    }
    
    private String extractJsonString(String json, String key) {
        int pos = json.indexOf(key);
        if (pos == -1) return "";
        int start = json.indexOf("\"", pos + key.length()) + 1;
        int end = json.indexOf("\"", start);
        if (start == 0 || end == -1) return "";
        return json.substring(start, end);
    }

    private GreenfootImage getTileFromGid(int gid)
    {
        if (gid == 0) return null;
        
        // Try multiple tilesets first
        if (!tilesets.isEmpty()) {
            TilesetInfo matchingTileset = null;
            int matchedGid = -1;
            
            // Find the tileset with the highest firstgid that is <= gid
            for (int i = tilesets.size() - 1; i >= 0; i--) {
                if (gid >= tilesets.get(i).firstgid) {
                    matchingTileset = tilesets.get(i);
                    matchedGid = tilesets.get(i).firstgid;
                    break;
                }
            }
            
            if (matchingTileset != null) {
                int index = gid - matchingTileset.firstgid;
                if (index >= 0 && index < matchingTileset.tiles.size()) {
                    return new GreenfootImage(matchingTileset.tiles.get(index));
                }
                // Gid is in this tileset's range, but index is out of bounds - return blank
                GreenfootImage blank = new GreenfootImage(tileSize, tileSize);
                blank.setColor(new Color(0, 0, 0, 0)); // Transparent
                return blank;
            }
            
            // No matching tileset found - this shouldn't happen if tilesets are loaded correctly
            DebugLog.log("WARNING: GID " + gid + " doesn't match any tileset (available: " + 
                tilesets.get(0).firstgid + "-" + (tilesets.get(tilesets.size()-1).firstgid + tilesets.get(tilesets.size()-1).tiles.size()) + ")");
        }
        
        // Fallback to single tileset
        int index = gid - tilesetFirstGid;
        if (tileset != null && !tileCache.isEmpty())
        {
            if (index >= 0 && index < tileCache.size())
            {
                return new GreenfootImage(tileCache.get(index));
            }
        }

        // Final fallback - transparent tile
        GreenfootImage fallback = new GreenfootImage(tileSize, tileSize);
        fallback.setColor(new Color(0, 0, 0, 0));
        fallback.fillRect(0, 0, tileSize, tileSize);
        return fallback;
    }
    
    /**
     * Apply flip/rotation transformations to a tile according to Tiled's specification
     * flipD (diagonal) is applied first as a transpose, then flipH and flipV
     */
    private GreenfootImage applyTileTransform(GreenfootImage original, boolean flipH, boolean flipV, boolean flipD)
    {
        GreenfootImage transformed = new GreenfootImage(original);
        
        // Step 1: Apply diagonal flip (transpose) if set
        if (flipD)
        {
            transformed = transposeTile(transformed);
        }
        
        // Step 2: Apply horizontal flip if set
        if (flipH)
        {
            transformed.mirrorHorizontally();
        }
        
        // Step 3: Apply vertical flip if set
        if (flipV)
        {
            transformed.mirrorVertically();
        }
        
        return transformed;
    }
    
    /**
     * Transpose a tile (swap x and y coordinates) - this is the diagonal flip in Tiled
     */
    private GreenfootImage transposeTile(GreenfootImage original)
    {
        int size = original.getWidth(); // Assuming square tiles
        GreenfootImage transposed = new GreenfootImage(size, size);
        
        // Transpose by swapping x and y
        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                Color pixel = original.getColorAt(x, y);
                transposed.setColorAt(y, x, pixel);
            }
        }
        
        return transposed;
    }
    
    /**
     * Apply opacity to a tile by adjusting alpha channel
     */
    private GreenfootImage applyOpacity(GreenfootImage original, float opacity)
    {
        int size = original.getWidth();
        GreenfootImage result = new GreenfootImage(size, size);
        
        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                Color pixel = original.getColorAt(x, y);
                int alpha = (int)(pixel.getAlpha() * opacity);
                Color newPixel = new Color(pixel.getRed(), pixel.getGreen(), pixel.getBlue(), alpha);
                result.setColorAt(x, y, newPixel);
            }
        }
        
        return result;
    }

    private int extractTilesetFirstGid(String xmlOrJson)
    {
        if (xmlOrJson.contains("<tileset "))
        {
            int tsPos = xmlOrJson.indexOf("<tileset ");
            if (tsPos == -1) return 1; // fallback
            int value = extractIntAttr(xmlOrJson.substring(tsPos), "firstgid=\"", "\"");
            DebugLog.log("Extracted firstgid from XML: " + value);
            return value > 0 ? value : 1;
        }
        int pos = xmlOrJson.indexOf("\"firstgid\":");
        if (pos == -1) return 1;
        int value = extractJsonInt(xmlOrJson.substring(pos), "\"firstgid\":");
        DebugLog.log("Extracted firstgid from JSON: " + value);
        return value > 0 ? value : 1;
    }

    private void logLayerCounts(boolean isJson)
    {
        if (isJson)
        {
            DebugLog.log("TMJ layers: Floor non-zero=" + countNonZero(floor) +
                               " Objects=" + countNonZero(objectsA) +
                               " Objects1=" + countNonZero(objectsB));
        }
        else
        {
            DebugLog.log("TMX layers: Floor non-zero=" + countNonZero(floor) +
                               " Collision=" + countNonZero(objectsA) +
                               " Collision1=" + countNonZero(objectsB));
        }
    }

    public GreenfootImage getFullMapImage()
    {
        return new GreenfootImage(fullMapImage);
    }

    /**
     * Build and return a single-layer image by layer name (e.g. "Over-Player").
     * Returns null if the layer is not found.
     */
    public GreenfootImage getLayerImage(String layerName)
    {
        if (layerName == null) return null;

        int idx = -1;
        for (int i = 0; i < tileLayerNames.size(); i++)
        {
            if (layerName.equalsIgnoreCase(tileLayerNames.get(i)))
            {
                idx = i;
                break;
            }
        }

        if (idx == -1)
        {
            DebugLog.log("Layer not found: " + layerName);
            return null;
        }

        GreenfootImage img = new GreenfootImage(mapW * tileSize, mapH * tileSize);
        float opacity = idx < tileLayerOpacity.size() ? tileLayerOpacity.get(idx) : 1.0f;
        drawLayerOnto(img, tileLayers.get(idx), tileLayerNames.get(idx), opacity);
        return img;
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
            DebugLog.log("  TMJ layer not found: " + layerName + ", returning empty layer");
            return new int[h][w];
        }
        
        // Find the opening brace of this layer object (search backward for {)
        int layerStart = json.lastIndexOf("{", namePos);
        if (layerStart == -1) {
            DebugLog.log("  TMJ layer object start not found: " + layerName);
            return new int[h][w];
        }
        
        // Find the closing brace of this layer object (search forward for })
        int layerEnd = json.indexOf("}", namePos);
        if (layerEnd == -1) {
            DebugLog.log("  TMJ layer object end not found: " + layerName);
            return new int[h][w];
        }
        
        // Extract just this layer's JSON object
        String layerJson = json.substring(layerStart, layerEnd + 1);
        
        // Now find "data" within this layer
        int dataStart = layerJson.indexOf("\"data\":[");
        if (dataStart == -1)
        {
            DebugLog.log("  TMJ data array not found in layer: " + layerName);
            return new int[h][w];
        }
        dataStart += 8; // Skip past "data":[
        
        // Find the end of the data array by looking for ],
        int dataEnd = layerJson.indexOf("],", dataStart);
        if (dataEnd == -1)
        {
            DebugLog.log("  TMJ data array end not found in layer: " + layerName);
            return new int[h][w];
        }
        
        String data = layerJson.substring(dataStart, dataEnd);
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
                        // Keep the raw value with flip flags - they'll be processed during rendering
                        layer[y][x] = raw;
                    }
                    idx++;
                }
            }
        }
        catch (Exception ex)
        {
            DebugLog.log("  TMJ parse error in layer: " + layerName + ": " + ex.getMessage());
        }
        return layer;
    }

    /**
     * Parse every tilelayer in the TMJ/JSON file in the order they appear.
     * Stores results in tileLayers / tileLayerNames for rendering all layers.
     */
    private void parseAllJsonTileLayers(String json, int w, int h)
    {
        int searchPos = json.indexOf("\"layers\":[");
        if (searchPos == -1)
        {
            DebugLog.log("No layers array found in TMJ");
            return;
        }

        int layerCount = 0;
        while (true)
        {
            // Find next tilelayer
            int tilePos = json.indexOf("\"type\":\"tilelayer\"", searchPos);
            if (tilePos == -1) break;

            // Extract layer name (search backward from tilePos)
            int namePos = json.lastIndexOf("\"name\":\"", tilePos);
            String layerName = "Layer";
            if (namePos != -1)
            {
                int nameStart = namePos + "\"name\":\"".length();
                int nameEnd = json.indexOf('"', nameStart);
                if (nameEnd > nameStart) layerName = json.substring(nameStart, nameEnd);
            }
            
            // Extract opacity (search forward from namePos)
            float opacity = 1.0f;
            int opacityPos = json.indexOf("\"opacity\":", namePos);
            if (opacityPos != -1 && opacityPos < tilePos + 500)
            {
                int opacityStart = opacityPos + "\"opacity\":".length();
                int opacityEnd = json.indexOf(",", opacityStart);
                if (opacityEnd == -1) opacityEnd = json.indexOf("}", opacityStart);
                try {
                    opacity = Float.parseFloat(json.substring(opacityStart, opacityEnd).trim());
                } catch (Exception e) {
                    opacity = 1.0f;
                }
            }

            int[][] layerData = parseJsonTileLayer(json, layerName, w, h);
            tileLayers.add(layerData);
            tileLayerNames.add(layerName);
            tileLayerOpacity.add(opacity);
            
            DebugLog.log("  Parsed tilelayer #" + (++layerCount) + ": " + layerName + " (opacity: " + opacity + ")");

            searchPos = tilePos + 1;
        }

        if (tileLayers.isEmpty())
        {
            DebugLog.log("Warning: no tile layers parsed; creating empty floor");
            tileLayers.add(new int[h][w]);
            tileLayerNames.add("Floor");
        }
        DebugLog.log("Total tilelayers found: " + layerCount);
    }

    private void buildSolidFromObjectLayer(String json)
    {
        int layerPos = json.indexOf("\"name\":\"Collision\"");
        if (layerPos == -1) {
            DebugLog.log("No Collision object layer found in TMJ");
            return;
        }
        int objsStart = json.indexOf("\"objects\":[", layerPos);
        if (objsStart == -1) return;
        int objsEnd = json.indexOf(']', objsStart);
        String objs = json.substring(objsStart + 10, objsEnd);
        String[] entries = objs.split("\\},\\s*\\{");
        
        DebugLog.log("Parsing Collision object layer with " + entries.length + " objects...");
        
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
            
            DebugLog.log("  Collision rect " + collisionRects.size() + ": x=" + rectX + " y=" + rectY + 
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
        DebugLog.log("Loaded " + collisionRects.size() + " collision rectangles from Collision layer");
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
