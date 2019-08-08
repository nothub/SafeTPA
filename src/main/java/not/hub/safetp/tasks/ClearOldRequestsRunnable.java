package not.hub.safetp.tasks;

import not.hub.safetp.SafeTP;
import org.bukkit.scheduler.BukkitRunnable;

public class ClearOldRequestsRunnable extends BukkitRunnable {

    private final SafeTP plugin;

    public ClearOldRequestsRunnable(SafeTP plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.clearOldRequests();
    }

}
