package lol.hub.safetpa.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import lol.hub.safetpa.util.Players;
import lol.hub.safetpa.Plugin;
import lol.hub.safetpa.RequestManager;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

// tpn (tpdeny)
public class DenyCmd extends TpCommand {
    public DenyCmd(Plugin plugin, PluginCommand pluginCommand) {
        super(plugin, pluginCommand);
    }

    @Override
    public boolean run(Player tpTarget, String requesterName) {
        var tpRequester = Players.getOnlinePlayer(plugin.getServer(), requesterName);
        if (tpRequester == null) {
            tpTarget.sendMessage(
                Component.text("Player ", NamedTextColor.GOLD)
                    .append(Component.text(requesterName))
                    .append(Component.text(" is not online.", NamedTextColor.GOLD))
            );
            return false;
        }

        if (!RequestManager.isRequestActive(tpTarget, tpRequester)) {
            tpTarget.sendMessage(
                Component.text("There is no request to deny from ", NamedTextColor.GOLD)
                    .append(Component.text(tpRequester.getName()))
                    .append(Component.text("!", NamedTextColor.GOLD))
            );
            return false;
        }

        tpTarget.sendMessage(
            Component.text("Request from ", NamedTextColor.GOLD)
                .append(Component.text(tpRequester.getName()))
                .append(Component.text(" denied", NamedTextColor.RED))
                .append(Component.text("!", NamedTextColor.GOLD))
        );
        tpRequester.sendMessage(
            Component.text("Your request sent to ", NamedTextColor.GOLD)
                .append(Component.text(tpTarget.getName()))
                .append(Component.text(" was", NamedTextColor.GOLD))
                .append(Component.text(" denied", NamedTextColor.RED))
                .append(Component.text("!", NamedTextColor.GOLD))
        );

        // TODO: wrap this method in a "deny" call
        RequestManager.removeRequests(tpTarget, tpRequester);

        return true;
    }
}
