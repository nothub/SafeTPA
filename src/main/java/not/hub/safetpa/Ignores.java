package not.hub.safetpa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public final class Ignores {
    private Ignores() {
        throw new java.lang.UnsupportedOperationException(Ignores.class.getCanonicalName() + " is a utility class and cannot be instantiated!");
    }

    private static final Gson gson = new GsonBuilder().create();
    private static final Type type = TypeToken.getParameterized(Set.class, UUID.class).getType();

    private static Path filePath(UUID player) {
        return Config.ignoresPath().resolve(player.toString() + ".json");
    }

    private static Set<UUID> load(UUID player) {
        try {
            var reader = Files.newBufferedReader(filePath(player));
            return gson.fromJson(reader, type);
        } catch (FileNotFoundException | NoSuchFileException ex) {
            return new HashSet<>(1);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new HashSet<>(1);
        }
    }

    private static void save(UUID player, Set<UUID> ignores) {
        var path = filePath(player);

        // create directory structure up to parent
        path.toFile().getParentFile().mkdirs();

        var json = gson.toJson(ignores);
        try {
            Files.writeString(path, json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static final Function<JavaPlugin, String> defaultPath = (plugin -> plugin.getDataFolder().toPath().resolve("ignores").toString());

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
