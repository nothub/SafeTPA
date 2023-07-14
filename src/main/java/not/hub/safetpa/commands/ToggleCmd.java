package not.hub.safetpa.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import not.hub.safetpa.Plugin;
import not.hub.safetpa.RequestManager;
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
            commandSender.sendMessage(
                Component.text("Request are now ", NamedTextColor.GOLD)
                    .append(Component.text(" enabled", NamedTextColor.GREEN))
                    .append(Component.text("!", NamedTextColor.GOLD))
            );
        } else {
            plugin.getConfig().set(BLOCKED_PREFIX + commandSender.getUniqueId(), true);
            RequestManager.cancelRequestsByTarget(commandSender);
            commandSender.sendMessage(
                Component.text("Request are now ", NamedTextColor.GOLD)
                    .append(Component.text(" disabled", NamedTextColor.RED))
                    .append(Component.text("!", NamedTextColor.GOLD))
            );
        }
        plugin.saveConfig();
        return true;
    }

}
