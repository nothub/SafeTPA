package not.hub.safetp;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class LocationUtils {

    @NotNull
    public static Vector getOverworldXzVector(Player requester) {
        return new Vector(
                Math.abs(requester.getLocation().getX()) * (requester.getWorld().getEnvironment().equals(World.Environment.NETHER) ? 8 : 1),
                0,
                Math.abs(requester.getLocation().getZ()) * (requester.getWorld().getEnvironment().equals(World.Environment.NETHER) ? 8 : 1)
        );
    }

}
