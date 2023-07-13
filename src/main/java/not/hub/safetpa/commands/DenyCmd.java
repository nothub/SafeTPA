package not.hub.safetpa.commands;

import not.hub.safetpa.util.Players;
import not.hub.safetpa.Plugin;
import not.hub.safetpa.RequestManager;
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
            tpTarget.sendMessage(ChatColor.GOLD + "Player " + ChatColor.RESET + requesterName + ChatColor.GOLD + " is not online.");
            return false;
        }

        if (!RequestManager.isRequestActive(tpTarget, tpRequester)) {
            tpTarget.sendMessage(ChatColor.GOLD + "There is no request to deny from " + ChatColor.RESET + tpRequester.getName() + ChatColor.GOLD + "!");
            return false;
        }

        tpTarget.sendMessage(ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getName() + ChatColor.RESET + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        tpRequester.sendMessage(ChatColor.GOLD + "Your request sent to " + ChatColor.RESET + tpTarget.getName() + ChatColor.RESET + ChatColor.GOLD + " was" + ChatColor.RED + " denied" + ChatColor.GOLD + "!");

        // TODO: wrap this method in a "deny" call
        RequestManager.removeRequests(tpTarget, tpRequester);

        return true;
    }
}
