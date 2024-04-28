package lol.hub.safetpa.commands;

import lol.hub.safetpa.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import lol.hub.safetpa.Ignores;
import lol.hub.safetpa.Log;
import lol.hub.safetpa.Plugin;
import lol.hub.safetpa.RequestManager;
import lol.hub.safetpa.util.Players;
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
            commandSender.sendMessage(Component.text("Player not found.", NamedTextColor.GOLD));
            return false;
        }

        if (Ignores.get(target.getUniqueId(), commandSender.getUniqueId())) {
            commandSender.sendMessage(
                Component.text(target.getName()).append(Component.text(" is ignoring your tpa requests!"))
            );
        }

        if (Config.spawnTpDeny() && Players.isAtSpawn(commandSender)) {
            Log.info("Denying teleport request while in spawn area from " + commandSender.getName() + " to " + target.getName());
            commandSender.sendMessage(
                Component.text("You are not allowed to teleport while in the spawn area!", NamedTextColor.GOLD)
            );
            return false;
        }

        if (plugin.isRequestBlock(target)) {
            commandSender.sendMessage(
                Component.text(target.getName())
                    .append(Component.text(" is currently not accepting any teleport requests!", NamedTextColor.GOLD))
            );
            return false;
        }

        if (Config.distanceLimit() &&
            Players.getOverworldXzVector(commandSender).distance(Players.getOverworldXzVector(target)) > Config.distanceLimitRadius()) {
            Log.info("Denying teleport request while out of range from " + commandSender.getName() + " to " + target.getName());
            commandSender.sendMessage(
                Component.text("You are too far away from ", NamedTextColor.GOLD)
                    .append(Component.text(target.getName()))
                    .append(Component.text(" to teleport!", NamedTextColor.GOLD))
            );
            return false;
        }

        if (RequestManager.isRequestActive(target, commandSender)) {
            commandSender.sendMessage(
                Component.text("Please wait for ", NamedTextColor.GOLD)
                    .append(Component.text(target.getName()))
                    .append(Component.text(" to accept or deny your request.", NamedTextColor.GOLD))
            );
            return false;
        }

        if (!Config.allowMultiTargetRequest() && RequestManager.isRequestActiveByRequester(commandSender)) {
            commandSender.sendMessage(
                Component.text("Please wait for your existing request to be accepted or denied.", NamedTextColor.GOLD)
            );
            return false;
        }

        commandSender.sendMessage(
            Component.text("Request sent to: ", NamedTextColor.GOLD).append(Component.text(target.getName()))
        );

        target.sendMessage(
            Component.text(commandSender.getName())
                .append(Component.text(" wants to teleport to you, ", NamedTextColor.GOLD))
                .append(
                    Component.text("[ACCEPT]", NamedTextColor.GREEN)
                        .hoverEvent(Component.text("Accept the teleport").asHoverEvent())
                        .clickEvent(ClickEvent.suggestCommand("/tpy " + commandSender.getName()))
                )
                .append(Component.text(" or ", NamedTextColor.GOLD))
                .append(
                    Component.text("[DENY]", NamedTextColor.RED)
                        .hoverEvent(Component.text("Deny the teleport").asHoverEvent())
                        .clickEvent(ClickEvent.suggestCommand("/tpn " + commandSender.getName()))
                )
                .append(Component.text(" or ", NamedTextColor.GOLD))
                .append(
                    Component.text("[IGNORE]", NamedTextColor.GRAY)
                        .hoverEvent(Component.text("Ignore the requester").asHoverEvent())
                        .clickEvent(ClickEvent.suggestCommand("/tpi " + commandSender.getName()))
                )
        );

        RequestManager.addRequest(target, commandSender);

        return true;
    }
}
