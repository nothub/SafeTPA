package lol.hub.safetpa;

import de.myzelyam.api.vanish.VanishAPI;
import lol.hub.safetpa.commands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import lol.hub.safetpa.commands.*;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Plugin extends JavaPlugin {
    public static final String BLOCKED_PREFIX = "requests-blocked-";
    private static final Predicate<org.bukkit.Server> superVanishLoaded = (server) ->
        server.getPluginManager().isPluginEnabled("SuperVanish") ||
            server.getPluginManager().isPluginEnabled("PremiumVanish");
    private final Map<String, TpCommand> commands = new HashMap<>();

    private static boolean shouldTpLeashed(Entity playerA, Entity playerB) {
        //Checks if the dimension is the same, otherwise checks if interdimensional leash tp is allowed
        return playerA.getWorld().getEnvironment() == playerB.getWorld().getEnvironment() || Config.includeLeashedInterdimensional();
    }

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

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerMove(PlayerMoveEvent event) {
                if (!Config.movementCheck()) return;
                if (!event.hasChangedPosition()) return;
                RequestManager.cancelRequestsByRequester(event.getPlayer());
            }
        }, this);

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
            tpTarget.sendMessage(
                Component.text("Teleporting ", NamedTextColor.GOLD)
                    .append(Component.text(tpRequester.getName()))
                    .append(Component.text(" in ", NamedTextColor.GOLD))
                    .append(Component.text(tpDelay))
                    .append(Component.text(" seconds...", NamedTextColor.GOLD)));
            tpRequester.sendMessage(
                Component.text("Teleporting in ", NamedTextColor.GOLD)
                    .append(Component.text(tpDelay))
                    .append(Component.text(" seconds...", NamedTextColor.GOLD)));

            int taskId = getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                if (RequestManager.isRequestActive(tpTarget, tpRequester)) executeTPMove(tpTarget, tpRequester);
            }, tpDelay * 20L);

            if (taskId == -1) {
                // TODO: handle error case: scheduling failed
            }
        } else {
            executeTPMove(tpTarget, tpRequester);
        }
    }

    public void executeTPMove(Player tpTarget, Player tpRequester) {
        Log.info("Teleporting " + tpRequester.getName() + " to " + tpTarget.getName());

        if (superVanishLoaded.test(getServer())) VanishAPI.hidePlayer(tpRequester);

        // TODO: Write flag to player nbt in case some exploit prevents
        //  the unvanish, so we can do the unvanish on the next login.

        if (Config.includeLeashed() && shouldTpLeashed(tpTarget, tpRequester)) {
            tpRequester.getWorld()
                .getNearbyEntities(tpRequester.getLocation(), 16, 16, 16).stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(LivingEntity::isLeashed)
                .filter(e -> e.getLeashHolder().getUniqueId().equals(tpRequester.getUniqueId()))
                .forEach(entity -> entity.teleportAsync(tpTarget.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN));
        }

        // execute teleport
        tpRequester.teleportAsync(tpTarget.getLocation(), PlayerTeleportEvent.TeleportCause.COMMAND)
            .thenAccept(result -> {
                if (result) {
                    tpTarget.sendMessage(
                        Component.text(tpRequester.getName())
                            .append(Component.text(" teleported to you!", NamedTextColor.GOLD)));
                    tpRequester.sendMessage(
                        Component.text("Teleported to ", NamedTextColor.GOLD)
                            .append(Component.text(tpTarget.getName()))
                            .append(Component.text("!", NamedTextColor.GOLD)));
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
