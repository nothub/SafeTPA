package not.hub.safetpa.tasks;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import not.hub.safetpa.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressFBWarnings
public class UnvanishRunnable extends BukkitRunnable {

    private final Plugin plugin;
    private final Player player;

    public UnvanishRunnable(Plugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        plugin.unvanish(player);
    }

}
