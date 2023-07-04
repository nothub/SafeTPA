package not.hub.safetpa.listeners;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import not.hub.safetpa.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class MoveListener implements Listener {

    private final Plugin plugin;

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public MoveListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        event.getPlayer().getMetadata("safetpa-tpid").forEach(meta -> {
            int taskId = meta.asInt();

            if (!scheduler.isCurrentlyRunning(taskId) && scheduler.isQueued(taskId)) {
                scheduler.cancelTask(taskId);

                Plugin.sendMessage(event.getPlayer(), ChatColor.RED + "Teleport failed!");
                plugin.requestManager().getRequestByRequester(event.getPlayer()).ifPresent(request -> {
                    Player target = plugin.getServer().getPlayer(request.target().uuid());
                    if (target != null) {
                        Plugin.sendMessage(target, ChatColor.GOLD + "Teleport failed!");
                    }
                });
                plugin.requestManager().removeRequestsByRequester(event.getPlayer());
            }
        });
        event.getPlayer().removeMetadata("safetpa-tpid", plugin);
    }

}
