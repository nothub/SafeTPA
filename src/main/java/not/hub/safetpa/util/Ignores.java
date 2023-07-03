package not.hub.safetpa.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class Ignores {
    private static final Gson gson = new GsonBuilder().create();
    private static final Type type = TypeToken.getParameterized(Set.class, UUID.class).getType();
    public static Path dir = Path.of("plugins").resolve("SafeTPA").resolve("ignores");

    private static Path path(UUID player) {
        return dir.resolve(player.toString() + ".json");
    }

    private static Set<UUID> load(UUID player) {
        try {
            var reader = Files.newBufferedReader(path(player));
            return gson.fromJson(reader, type);
        } catch (FileNotFoundException ex) {
            return new HashSet<>(1);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new HashSet<>(1);
        }
    }

    private static void save(UUID player, Set<UUID> ignores) {
        var path = path(player);

        // create directory structure up to parent
        path.toFile().getParentFile().mkdirs();

        var json = gson.toJson(ignores);
        try {
            Files.write(path, json.getBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean get(UUID player, UUID target) {
        return load(player).contains(target);
    }

    /**
     * @return true: success, false: maximum ignores reached
     */
    public static boolean set(UUID player, UUID target, boolean ignore) {
        Set<UUID> set = load(player);
        if (ignore) {
            if (set.size() >= 1024) return false;
            set.add(target);
        } else {
            set.remove(target);
        }
        save(player, set);
        return true;
    }
}
