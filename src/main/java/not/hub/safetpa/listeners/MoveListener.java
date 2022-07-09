package not.hub.safetpa.listeners;

import lombok.RequiredArgsConstructor;
import not.hub.safetpa.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;

@RequiredArgsConstructor
public class MoveListener implements Listener {
    private final Plugin plugin;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        if (event.getFrom().getX() != event.getTo().getX()
            || event.getFrom().getY() != event.getTo().getY()
            || event.getFrom().getZ() != event.getTo().getZ()
            || event.getFrom().getWorld() != event.getTo().getWorld()) {
            event.getPlayer().getMetadata("safetpa-tpid").forEach(meta -> {
                int taskId = meta.asInt();

                if (!scheduler.isCurrentlyRunning(taskId) && scheduler.isQueued(taskId)) {
                    scheduler.cancelTask(taskId);

                    Plugin.sendMessage(event.getPlayer(), ChatColor.RED + "Teleport failed!");
                    plugin.getRequestManager().getRequestByRequester(event.getPlayer()).ifPresent(request -> {
                        Player target = plugin.getServer().getPlayer(request.target().left());
                        if (target != null) {
                            Plugin.sendMessage(target, ChatColor.GOLD + "Teleport failed!");
                        }
                    });
                    plugin.getRequestManager().removeRequestsByRequester(event.getPlayer());
                }
            });
            event.getPlayer().removeMetadata("safetpa-tpid", plugin);
        }
    }
}
