package not.hub.safetpa;

import de.myzelyam.api.vanish.VanishAPI;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import not.hub.safetpa.commands.*;
import not.hub.safetpa.listeners.MoveListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class Plugin extends JavaPlugin {
    public static final String BLOCKED_PREFIX = "requests-blocked-";
    private static final Predicate<org.bukkit.Server> superVanishLoaded = (server) ->
        server.getPluginManager().isPluginEnabled("SuperVanish") ||
            server.getPluginManager().isPluginEnabled("PremiumVanish");
    private final Map<String, TpCommand> commands = new HashMap<>();

    public Set<PluginCommand> getPluginCommands() {
        return getServer()
            .getCommandMap()
            .getKnownCommands()
            .values()
            .stream()
            .filter(org.bukkit.command.Command::isRegistered)
            .filter(cmd -> cmd instanceof PluginCommand)
            .map(cmd -> (PluginCommand) cmd)
            .filter(cmd -> cmd.getPlugin() == this)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void onEnable() {
        Log.set(getLogger());

        PaperLib.suggestPaper(this);

        new Metrics(this, 11798);

        Config.load(this);

        for (PluginCommand cmd : getPluginCommands()) {
            switch (cmd.getLabel()) {
                case "tpa" -> commands.put("tpa", new AskCmd(this, cmd));
                case "tpy" -> commands.put("tpy", new AcceptCmd(this, cmd));
                case "tpn" -> commands.put("tpn", new DenyCmd(this, cmd));
                case "tpt" -> commands.put("tpt", new ToggleCmd(this, cmd));
                case "tpi" -> commands.put("tpi", new IgnoreCmd(this, cmd));
                default -> throw new IllegalStateException("Unknown command: " + cmd.getLabel());
            }
        }

        getServer().getPluginManager().registerEvents(new MoveListener(this), this);

        getServer().getScheduler().runTaskTimer(this, this::clearOldRequests, 20, 20);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
        if (!(commandSender instanceof Player sender)) {
            Log.warn("Ignoring command executed by non-player sender: " + commandSender.getName());
            return false;
        }

        if (!commands.containsKey(commandLabel)) {
            Log.warn("Unknown command: " + commandLabel);
            return false;
        }

        String targetName = args.length > 0 ? args[0] : null;

        // make sure that sender and target are not the same player
        if (targetName != null && sender.getName().equalsIgnoreCase(targetName)) {
            commands.get(commandLabel).sendUsage(sender);
            return false;
        }

        return commands.get(commandLabel).run(sender, targetName);
    }

    public void clearOldRequests() {
        RequestManager.clearOldRequests(Config.requestTimeoutSeconds());
    }

    public void executeTP(Player tpTarget, Player tpRequester) {
        if (tpTarget == null || tpRequester == null) {
            return;
        }

        // deny mounted target or requester
        if (tpTarget.getVehicle() != null || tpRequester.getVehicle() != null) {
            TextComponent msg = Component.text("Teleport failed!", NamedTextColor.RED);
            tpTarget.sendMessage(msg);
            tpRequester.sendMessage(msg);
            return;
        }

        int tpDelay = Config.tpDelaySeconds();
        if (tpDelay > 0) {
            tpTarget.sendMessage(ChatColor.GOLD + "Teleporting " + tpRequester.getName() + " in " + ChatColor.RESET + tpDelay + ChatColor.GOLD + " seconds...");
            tpRequester.sendMessage(ChatColor.GOLD + "Teleporting in " + ChatColor.RESET + tpDelay + ChatColor.GOLD + " seconds...");
            int taskId = getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                executeTPMove(tpTarget, tpRequester), tpDelay * 20L);

            tpRequester.setMetadata("safetpa-tpid", new FixedMetadataValue(this, taskId));
        } else {
            executeTPMove(tpTarget, tpRequester);
        }
    }

    public void executeTPMove(Player tpTarget, Player tpRequester) {
        Log.info("Teleporting " + tpRequester.getName() + " to " + tpTarget.getName());

        if (superVanishLoaded.test(getServer())) VanishAPI.hidePlayer(tpRequester);

        // TODO: Write flag to player nbt in case some exploit prevents
        //  the unvanish, so we can do the unvanish on the next login.

        // execute teleport
        PaperLib.teleportAsync(tpRequester, tpTarget.getLocation())
            .thenAccept(result -> {
                if (result) {
                    tpTarget.sendMessage(tpRequester.getName() + ChatColor.RESET + ChatColor.GOLD + " teleported to you!");
                    tpRequester.sendMessage(ChatColor.GOLD + "Teleported to " + ChatColor.RESET + tpTarget.getName() + ChatColor.RESET + ChatColor.GOLD + "!");
                } else {
                    TextComponent msg = Component.text("Teleport failed, you should harass your admin because of this!", NamedTextColor.RED);
                    tpTarget.sendMessage(msg);
                    tpRequester.sendMessage(msg);
                }
            })
            .thenAccept(ignored -> {
                // Unvanish requester after n ticks
                getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                    if (superVanishLoaded.test(getServer())) VanishAPI.showPlayer(tpRequester);
                }, Config.unvanishDelayTicks());
            });
    }

    public boolean isRequestBlock(Player player) {
        // TODO: stop doing this, use player metadata or sqlite instead
        return getConfig().getBoolean(BLOCKED_PREFIX + player.getUniqueId());
    }

}
