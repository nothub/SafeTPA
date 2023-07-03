package not.hub.safetpa.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

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
    private static Path dir = null;

    public static void setDir(Path path) {
        dir = path;
    }

    private static Path path(UUID player) {
        return dir.resolve(player.toString() + ".json");
    }

    private static Set<UUID> load(UUID player) {
        try {
            var reader = Files.newBufferedReader(path(player));
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            // TODO: handle error
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
        } catch (IOException e) {
            // TODO: handle error
        }
    }

    public static boolean isIgnored(UUID player, UUID target) {
        return load(player).contains(target);
    }

    public static void setIgnored(UUID player, UUID target, boolean ignore) {
        Set<UUID> set = load(player);
        if (ignore) {
            set.add(target);
        } else {
            set.remove(target);
        }
        save(player, set);
    }
}
