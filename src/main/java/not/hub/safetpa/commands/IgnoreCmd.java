package not.hub.safetpa.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import not.hub.safetpa.Ignores;
import not.hub.safetpa.util.Players;
import not.hub.safetpa.Plugin;
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
            commandSender.sendMessage(
                Component.text("Player ", NamedTextColor.GOLD)
                    .append(Component.text(targetName))
                    .append(Component.text(" not found.", NamedTextColor.GOLD))
            );
            return false;
        }

        if (Ignores.get(commandSender.getUniqueId(), targetUuid)) {
            Ignores.set(commandSender.getUniqueId(), targetUuid, false);
            commandSender.sendMessage(Component.text("No longer ignoring tp requests from ").append(Component.text(targetName)));
        } else {
            boolean success = Ignores.set(commandSender.getUniqueId(), targetUuid, true);
            if (success) {
                commandSender.sendMessage(Component.text("Ignoring tp requests from ").append(Component.text(targetName)));
            } else {
                commandSender.sendMessage(Component.text("Maximum reached, can not add more ignores!", NamedTextColor.RED));
            }
        }

        return true;
    }

}
