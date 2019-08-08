package not.hub.safetp.tasks;

import not.hub.safetp.SafeTP;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class UnvanishRunnable extends BukkitRunnable {

    private final SafeTP plugin;
    private final Player player;

    public UnvanishRunnable(SafeTP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.unvanish(player);
    }

}
