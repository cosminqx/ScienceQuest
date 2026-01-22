import java.awt.Font;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * FontManager
 * -----------
 * Uses Greenfoot reflection hack to inject custom TTF fonts.
 * This is the ONLY reliable method in older Greenfoot versions.
 */
public class FontManager {

    private static greenfoot.Font pixeled;
    private static greenfoot.Font pixeledSmall;
    private static greenfoot.Font pixeledLarge;

    public static void loadFonts() {
        try {
            // Create dummy Greenfoot fonts
            pixeled = new greenfoot.Font("Arial", false, false, 10);
            pixeledSmall = new greenfoot.Font("Arial", false, false, 8);
            pixeledLarge = new greenfoot.Font("Arial", false, false, 12);

            // Load TTF from project
            InputStream is = FontManager.class
                    .getClassLoader()
                    .getResourceAsStream("fonts/Pixeled.ttf");

            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, is);

            Font big = awtFont.deriveFont(10f);
            Font small = awtFont.deriveFont(8f);
            Font large = awtFont.deriveFont(12f);

            // Access Greenfoot internal font field
            Field internalFont =
                    greenfoot.Font.class.getDeclaredField("font");

            internalFont.setAccessible(true);

            internalFont.set(pixeled, big);
            internalFont.set(pixeledSmall, small);
            internalFont.set(pixeledLarge, large);

        } catch (Exception e) {
            System.out.println("Pixel font injection failed — fallback used");
        }
    }

    public static greenfoot.Font getPixeled() {
        return pixeled;
    }

    public static greenfoot.Font getPixeledSmall() {
        return pixeledSmall;
    }
    
    public static greenfoot.Font getPixeledLarge() {
        return pixeledLarge;
    }
}
