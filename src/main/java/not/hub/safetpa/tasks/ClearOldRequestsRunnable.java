package not.hub.safetpa.tasks;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import not.hub.safetpa.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressFBWarnings
public class ClearOldRequestsRunnable extends BukkitRunnable {

    private final Plugin plugin;

    public ClearOldRequestsRunnable(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.clearOldRequests();
    }

}
