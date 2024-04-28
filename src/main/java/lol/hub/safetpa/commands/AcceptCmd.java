package lol.hub.safetpa.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import lol.hub.safetpa.util.Players;
import lol.hub.safetpa.Plugin;
import lol.hub.safetpa.RequestManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

// tpy (tpaccept)
public class AcceptCmd extends TpCommand {
    public AcceptCmd(Plugin plugin, PluginCommand pluginCommand) {
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
                Component.text("There is no request to accept from ", NamedTextColor.GOLD)
                    .append(Component.text(tpRequester.getName()))
                    .append(Component.text("!", NamedTextColor.GOLD))
            );
            return false;
        }

        tpTarget.sendMessage(
            Component.text("Request from ", NamedTextColor.GOLD)
                .append(Component.text(tpRequester.getName()))
                .append(Component.text(" accepted", NamedTextColor.GREEN))
                .append(Component.text("!", NamedTextColor.GOLD))
        );
        tpRequester.sendMessage(
            Component.text("Your request was ", NamedTextColor.GOLD)
                .append(Component.text("accepted", NamedTextColor.GREEN))
                .append(Component.text(", teleporting to: ", NamedTextColor.GOLD))
                .append(Component.text(tpTarget.getName()))
        );

        // TODO: combine these 2 methods to a single "accept" call
        plugin.executeTP(tpTarget, tpRequester);
        RequestManager.removeRequests(tpTarget, tpRequester);

        return true;
    }
}
