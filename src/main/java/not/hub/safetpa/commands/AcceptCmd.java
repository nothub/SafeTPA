package not.hub.safetpa.commands;

import not.hub.safetpa.util.Players;
import not.hub.safetpa.Plugin;
import not.hub.safetpa.RequestManager;
import org.bukkit.ChatColor;
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
            tpTarget.sendMessage(ChatColor.GOLD + "Player " + ChatColor.RESET + requesterName + ChatColor.GOLD + " is not online.");
            return false;
        }

        if (!RequestManager.isRequestActive(tpTarget, tpRequester)) {
            tpTarget.sendMessage(ChatColor.GOLD + "There is no request to accept from " + ChatColor.RESET + tpRequester.getName() + ChatColor.RESET + ChatColor.GOLD + "!");
            return false;
        }

        tpTarget.sendMessage(ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getName() + ChatColor.RESET + ChatColor.GREEN + " accepted" + ChatColor.GOLD + "!");
        tpRequester.sendMessage(ChatColor.GOLD + "Your request was " + ChatColor.GREEN + "accepted" + ChatColor.GOLD + ", teleporting to: " + ChatColor.RESET + tpTarget.getName());

        // TODO: combine these 2 methods to a single "accept" call
        plugin.executeTP(tpTarget, tpRequester);
        RequestManager.removeRequests(tpTarget, tpRequester);

        return true;
    }
}
