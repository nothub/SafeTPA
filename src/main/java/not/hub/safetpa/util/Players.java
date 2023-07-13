package not.hub.safetpa.util;

import not.hub.safetpa.Config;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.regex.Pattern;

public class Players {
    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]{1,16}$");

    public static boolean validName(String name) {
        if (name == null) {
            return false;
        }
        return pattern.matcher(name).matches();
    }

    public static Player getOnlinePlayer(Server server, String name) {
        return server.getOnlinePlayers().stream().filter(p -> p.getName().equals(name)).findAny().orElse(null);
    }

    public static UUID getPlayerUUID(Server server, String name) {
        var onlinePlayer = Players.getOnlinePlayer(server, name);
        if (onlinePlayer != null) return onlinePlayer.getUniqueId();

        var offlinePlayer = server.getOfflinePlayerIfCached(name);
        if (offlinePlayer != null) return offlinePlayer.getUniqueId();

        return null;
    }

    public static Vector getOverworldXzVector(Player requester) {
        return new Vector(
            Math.abs(requester.getLocation().getX()) * (requester.getWorld().getEnvironment() == World.Environment.NETHER ? 8 : 1),
            0.0,
            Math.abs(requester.getLocation().getZ()) * (requester.getWorld().getEnvironment() == World.Environment.NETHER ? 8 : 1)
        );
    }

    public static boolean isAtSpawn(Player requester) {
        // end spawn is not spawn
        if (requester.getWorld().getEnvironment() == World.Environment.THE_END) {
            return false;
        }
        Vector pos = getOverworldXzVector(requester);
        return pos.getX() <= Config.spawnTpDenyRadius() && pos.getZ() <= Config.spawnTpDenyRadius();
    }

}
