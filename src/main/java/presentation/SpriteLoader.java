package presentation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;


public final class SpriteLoader {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();

    private SpriteLoader() { }

    public static synchronized BufferedImage load(String name) {
        if (!CACHE.containsKey(name)) CACHE.put(name, read(name));
        return CACHE.get(name);
    }

    private static BufferedImage read(String name) {
        try (InputStream in = SpriteLoader.class.getResourceAsStream("/images/" + name)) {
            if (in != null) return ImageIO.read(in);
        } catch (IOException ignored) { }
        File file = new File("images", name);
        if (file.isFile()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) { }
        }
        return null;
    }
}
