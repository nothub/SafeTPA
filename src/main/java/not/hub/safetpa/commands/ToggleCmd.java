package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import static not.hub.safetpa.Plugin.BLOCKED_PREFIX;

// tpt (tptoggle)
public class ToggleCmd extends TpCommand {
    public ToggleCmd(Plugin plugin, PluginCommand pluginCommand) {
        super(plugin, pluginCommand);
    }

    @Override
    public boolean run(Player commandSender, String ignored) {
        if (plugin.isRequestBlock(commandSender)) {
            plugin.getConfig().set(BLOCKED_PREFIX + commandSender.getUniqueId(), null); // if toggle is getting turned off, we delete instead of setting false
            commandSender.sendMessage(ChatColor.GOLD + "Request are now " + ChatColor.GREEN + " enabled" + ChatColor.GOLD + "!");
        } else {
            plugin.getConfig().set(BLOCKED_PREFIX + commandSender.getUniqueId(), true);
            plugin.requestManager().removeRequestsByTarget(commandSender);
            commandSender.sendMessage(ChatColor.GOLD + "Request are now " + ChatColor.RED + " disabled" + ChatColor.GOLD + "!");
        }
        plugin.saveConfig();
        return true;
    }
}
