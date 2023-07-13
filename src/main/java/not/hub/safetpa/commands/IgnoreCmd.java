package not.hub.safetpa.commands;

import not.hub.safetpa.Ignores;
import not.hub.safetpa.Players;
import not.hub.safetpa.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

// tpi (tpignore)
public class IgnoreCmd extends TpCommand {
    public IgnoreCmd(Plugin plugin, PluginCommand pluginCommand) {
        super(plugin, pluginCommand);
    }

    @Override
    public boolean run(Player commandSender, String targetName) {
        var targetUuid = Players.getPlayerUUID(plugin.getServer(), targetName);
        if (targetUuid == null) {
            commandSender.sendMessage(ChatColor.GOLD + "Player " + ChatColor.RESET + targetName + ChatColor.GOLD + " not found.");
            return false;
        }

        if (Ignores.get(commandSender.getUniqueId(), targetUuid)) {
            Ignores.set(commandSender.getUniqueId(), targetUuid, false);
            commandSender.sendMessage("No longer ignoring tp requests from " + targetName);
        } else {
            boolean success = Ignores.set(commandSender.getUniqueId(), targetUuid, true);
            if (success) {
                commandSender.sendMessage("Ignoring tp requests from " + targetName);
            } else {
                commandSender.sendMessage(ChatColor.RED + "Maximum reached, can not add more ignores!");
            }
        }

        return true;
    }
}
