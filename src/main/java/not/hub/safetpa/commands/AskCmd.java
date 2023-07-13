package not.hub.safetpa.commands;

import not.hub.safetpa.*;
import not.hub.safetpa.util.Players;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

// tpa (tpask)
public class AskCmd extends TpCommand {
    public AskCmd(Plugin plugin, PluginCommand pluginCommand) {
        super(plugin, pluginCommand);
    }

    @Override
    public boolean run(Player commandSender, String targetName) {
        var target = Players.getOnlinePlayer(plugin.getServer(), targetName);
        if (target == null) {
            commandSender.sendMessage(ChatColor.GOLD + "Player not found.");
            return false;
        }

        if (Ignores.get(target.getUniqueId(), commandSender.getUniqueId())) {
            commandSender.sendMessage(target.getName() + " is ignoring your tpa requests!");
        }

        if (Config.spawnTpDeny() && Players.isAtSpawn(commandSender)) {
            Log.info("Denying teleport request while in spawn area from " + commandSender.getName() + " to " + target.getName());
            commandSender.sendMessage(ChatColor.GOLD + "You are not allowed to teleport while in the spawn area!");
            return false;
        }

        if (plugin.isRequestBlock(target)) {
            commandSender.sendMessage(target.getName() + ChatColor.RESET + ChatColor.GOLD + " is currently not accepting any teleport requests!");
            return false;
        }

        if (Config.distanceLimit() &&
            Players.getOverworldXzVector(commandSender).distance(Players.getOverworldXzVector(target)) > Config.distanceLimitRadius()) {
            Log.info("Denying teleport request while out of range from " + commandSender.getName() + " to " + target.getName());
            commandSender.sendMessage(ChatColor.GOLD + "You are too far away from " + ChatColor.RESET + target.getName() + ChatColor.RESET + ChatColor.GOLD + " to teleport!");
            return false;
        }

        if (RequestManager.isRequestActive(target, commandSender)) {
            commandSender.sendMessage(ChatColor.GOLD + "Please wait for " + ChatColor.RESET + target.getName() + ChatColor.RESET + ChatColor.GOLD + " to accept or deny your request.");
            return false;
        }

        if (!Config.allowMultiTargetRequest() && RequestManager.isRequestActiveByRequester(commandSender)) {
            commandSender.sendMessage(ChatColor.GOLD + "Please wait for your existing request to be accepted or denied.");
            return false;
        }

        commandSender.sendMessage(ChatColor.GOLD + "Request sent to: " + ChatColor.RESET + target.getName());
        target.sendMessage(commandSender.getName() + "" + ChatColor.GOLD + " wants to teleport to you.");
        target.sendMessage(ChatColor.GOLD + "Type " + ChatColor.RESET + "/tpy " + commandSender.getName() + ChatColor.RESET + ChatColor.GOLD + " to accept or " + ChatColor.RESET + "/tpn " + commandSender.getName() + ChatColor.GOLD + " to deny.");

        RequestManager.addRequest(target, commandSender);

        return true;
    }
}
