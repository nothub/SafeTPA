package not.hub.safetpa;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.papermc.lib.PaperLib;
import not.hub.safetpa.commands.*;
import not.hub.safetpa.listeners.MoveListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Plugin extends JavaPlugin {

    private static final String BLOCKED_PREFIX = "requests-blocked-";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[^a-zA-Z\\d_ ]");

    private final RequestManager requestManager = new RequestManager();

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public RequestManager requestManager() {
        return requestManager;
    }

    public static String sanitizeUsername(String name) {
        name = USERNAME_PATTERN.matcher(name).replaceAll("");
        if (name.length() < 1 || name.length() > 16) {
            return null;
        }
        return name;
    }

    Map<String, Command> commands = new HashMap<>();

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

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        new Metrics(this, 11798);

        Config.load(this);

        for (PluginCommand cmd : getPluginCommands()) {
            switch (cmd.getLabel()) {
                case "tpa" -> commands.put("tpa", new AskCmd(cmd));
                case "tpy" -> commands.put("tpy", new AcceptCmd(cmd));
                case "tpn" -> commands.put("tpn", new DenyCmd(cmd));
                case "tpt" -> commands.put("tpt", new ToggleCmd(cmd));
                case "tpi" -> commands.put("tpi", new IgnoreCmd(cmd));
                default -> throw new IllegalStateException("Unknown command: " + cmd.getLabel() + " " + cmd.getAliases());
            }
        }

        getServer().getPluginManager().registerEvents(new MoveListener(this), this);

        getServer().getScheduler().runTaskTimer(this, this::clearOldRequests, 20, 20);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull org.bukkit.command.Command command, @NotNull String commandLabel, String[] args) {
        if (!(commandSender instanceof Player sender))
            return true;

        // 0 arg commands

        if (command.getLabel().equalsIgnoreCase("tpt")) {
            toggleRequestBlock(sender);
            return true;
        }

        // 0 || 1 arg commands
        // TODO: move tpi here

        // > 0 arg commands

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "You need to run this command with an argument, like this:");
            sender.sendMessage("/tpa NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpy NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpn NAME" + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/ignoretp NAME");
            return false;
        }

        // If this check is passed (target is valid / if case is not executed),
        // args[0] is guranteed to contain the name of a player that currently is online.
        if (isInvalidTarget(getServer(), args[0])) {
            sender.sendMessage(ChatColor.GOLD + "Player not found.");
            return false;
        }
        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.GOLD + "Player not found.");
            return false;
        }

        if (sender.getName().equalsIgnoreCase(args[0])) {
            // Target is sender, we just do nothing.
            sender.sendMessage(ChatColor.GOLD + "Ignoring command because it does not make much sense!");
            return false;
        }

        if (command.getLabel().equalsIgnoreCase("tpa")) {
            askTP(target, sender);
            return true;
        }

        if (command.getLabel().equalsIgnoreCase("tpy")) {
            acceptTP(sender, target);
            return true;
        }

        if (command.getLabel().equalsIgnoreCase("tpn")) {
            denyTP(sender, target);
            return true;
        }

        if (command.getLabel().equalsIgnoreCase("tpi")) {
            ignoreTP(sender, target);
            return true;
        }

        return false;
    }

    private void ignoreTP(Player sender, Player target) {
        if (Ignores.get(sender.getUniqueId(), target.getUniqueId())) {
            Ignores.set(sender.getUniqueId(), target.getUniqueId(), false);
            sender.sendMessage("No longer ignoring tp requests from " + target.getName());
        } else {
            boolean success = Ignores.set(sender.getUniqueId(), target.getUniqueId(), true);
            if (success) {
                sender.sendMessage("Ignoring tp requests from " + target.getName());
            } else {
                sender.sendMessage(ChatColor.RED + "Maximum reached, can not add more ignores!");
            }
        }
    }

    public void clearOldRequests() {
        requestManager.clearOldRequests(Config.requestTimeoutSeconds());
    }

    private void askTP(Player tpTarget, Player tpRequester) {
        if (Ignores.get(tpTarget.getUniqueId(), tpRequester.getUniqueId())) {
            tpRequester.sendMessage(tpTarget.getName() + " is ignoring your tpa requests!");
        }

        if (Config.spawnTpDeny() && isAtSpawn(tpRequester)) {
            getLogger().info("Denying teleport request while in spawn area from " + tpRequester.getName() + " to " + tpTarget.getName());
            tpRequester.sendMessage(ChatColor.GOLD + "You are not allowed to teleport while in the spawn area!");
            return;
        }

        if (isRequestBlock(tpTarget)) {
            tpRequester.sendMessage(tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " is currently not accepting any teleport requests!");
            return;
        }

        if (Config.distanceLimit() &&
            getOverworldXzVector(tpRequester).distance(getOverworldXzVector(tpTarget)) > Config.distanceLimitRadius()) {
            getLogger().info("Denying teleport request while out of range from " + tpRequester.getName() + " to " + tpTarget.getName());
            tpRequester.sendMessage(ChatColor.GOLD + "You are too far away from " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to teleport!");
            return;
        }

        if (requestManager.isRequestActive(tpTarget, tpRequester)) {
            tpRequester.sendMessage(ChatColor.GOLD + "Please wait for " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to accept or deny your request.");
            return;
        }

        if (!Config.allowMultiTargetRequest() && requestManager.isRequestActiveByRequester(tpRequester)) {
            tpRequester.sendMessage(ChatColor.GOLD + "Please wait for your existing request to be accepted or denied.");
            return;
        }

        tpRequester.sendMessage(ChatColor.GOLD + "Request sent to: " + ChatColor.RESET + tpTarget.getDisplayName());
        tpTarget.sendMessage(tpRequester.getDisplayName() + "" + ChatColor.GOLD + " wants to teleport to you.");
        tpTarget.sendMessage(ChatColor.GOLD + "Type " + ChatColor.RESET + "/tpy " + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to accept or " + ChatColor.RESET + "/tpn " + tpRequester.getDisplayName() + ChatColor.GOLD + " to deny.");

        requestManager.addRequest(tpTarget, tpRequester);

    }

    private void acceptTP(Player tpTarget, Player tpRequester) {
        if (!requestManager.isRequestActive(tpTarget, tpRequester)) {
            tpTarget.sendMessage(ChatColor.GOLD + "There is no request to accept from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            return;
        }

        tpTarget.sendMessage(ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + " accepted" + ChatColor.GOLD + "!");
        tpRequester.sendMessage(ChatColor.GOLD + "Your request was " + ChatColor.GREEN + "accepted" + ChatColor.GOLD + ", teleporting to: " + ChatColor.RESET + tpTarget.getDisplayName());

        executeTP(tpTarget, tpRequester);
        requestManager.removeRequests(tpTarget, tpRequester);

    }

    private void denyTP(Player tpTarget, Player tpRequester) {
        if (!requestManager.isRequestActive(tpTarget, tpRequester)) {
            tpTarget.sendMessage(ChatColor.GOLD + "There is no request to deny from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            return;
        }

        tpTarget.sendMessage(ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        tpRequester.sendMessage(ChatColor.GOLD + "Your request sent to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " was" + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        requestManager.removeRequests(tpTarget, tpRequester);
    }

    private void executeTP(Player tpTarget, Player tpRequester) {
        if (tpTarget == null || tpRequester == null) {
            return;
        }

        // deny mounted target or requester
        if (tpTarget.getVehicle() != null || tpRequester.getVehicle() != null) {
            tpTarget.sendMessage(ChatColor.RED + "Teleport failed!");
            tpRequester.sendMessage(ChatColor.RED + "Teleport failed!");
            return;
        }

        int tpDelay = Config.tpDelaySeconds();
        if (tpDelay > 0) {
            tpTarget.sendMessage(ChatColor.GOLD + "Teleporting " + tpRequester.getDisplayName() + " in " + ChatColor.RESET + tpDelay + ChatColor.GOLD + " seconds...");
            tpRequester.sendMessage(ChatColor.GOLD + "Teleporting in " + ChatColor.RESET + tpDelay + ChatColor.GOLD + " seconds...");
            int taskId = getServer().getScheduler().scheduleSyncDelayedTask(this, () ->
                executeTPMove(tpTarget, tpRequester), tpDelay * 20L);

            tpRequester.setMetadata("safetpa-tpid", new FixedMetadataValue(this, taskId));
        } else {
            executeTPMove(tpTarget, tpRequester);
        }
    }

    private void executeTPMove(Player tpTarget, Player tpRequester) {
        getLogger().info("Teleporting " + tpRequester.getName() + " to " + tpTarget.getName());

        vanish(tpRequester);

        // execute teleport
        PaperLib.teleportAsync(tpRequester, tpTarget.getLocation()).thenAccept(result -> {
            if (result) {
                tpTarget.sendMessage(tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " teleported to you!");
                tpRequester.sendMessage(ChatColor.GOLD + "Teleported to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            } else {
                tpTarget.sendMessage(ChatColor.RED + "Teleport failed, you should harass your admin because of this!");
                tpRequester.sendMessage(ChatColor.RED + "Teleport failed, you should harass your admin because of this!");
            }
        }).thenAccept(ignored -> {
            // Unvanish requester after n ticks
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.unvanish(tpRequester), Config.unvanishDelayTicks());
        });
    }

    private void toggleRequestBlock(Player toggleRequester) {
        if (toggleRequester == null) {
            return;
        }

        if (isRequestBlock(toggleRequester)) {
            getConfig().set(BLOCKED_PREFIX + toggleRequester.getUniqueId(), null); // if toggle is getting turned off, we delete instead of setting false
            toggleRequester.sendMessage(ChatColor.GOLD + "Request are now " + ChatColor.GREEN + " enabled" + ChatColor.GOLD + "!");
        } else {
            getConfig().set(BLOCKED_PREFIX + toggleRequester.getUniqueId(), true);
            requestManager.removeRequestsByTarget(toggleRequester);
            toggleRequester.sendMessage(ChatColor.GOLD + "Request are now " + ChatColor.RED + " disabled" + ChatColor.GOLD + "!");
        }

        saveConfig();
    }

    private boolean isRequestBlock(Player player) {
        return getConfig().getBoolean(BLOCKED_PREFIX + player.getUniqueId());
    }

    private Vector getOverworldXzVector(Player requester) {
        return new Vector(
            Math.abs(requester.getLocation().getX()) * (requester.getWorld().getEnvironment() == World.Environment.NETHER ? 8 : 1),
            0.0,
            Math.abs(requester.getLocation().getZ()) * (requester.getWorld().getEnvironment() == World.Environment.NETHER ? 8 : 1)
        );
    }

    private boolean isAtSpawn(Player requester) {
        // end spawn is not spawn
        if (requester.getWorld().getEnvironment() == World.Environment.THE_END) {
            return false;
        }
        Vector pos = getOverworldXzVector(requester);
        return pos.getX() <= Config.spawnTpDenyRadius() && pos.getZ() <= Config.spawnTpDenyRadius();
    }

    private static boolean isInvalidTarget(Server server, String args) {
        // check for empty argument
        if (args.isEmpty()) {
            return true;
        }
        // check for invalid usernames
        String target = sanitizeUsername(args);
        if (target == null) {
            return true;
        }
        // check if player might be online
        return server.getPlayer(target) == null;
    }

    private void vanish(Player player) {
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.hidePlayer(this, player);
            }
        }
    }

    public void unvanish(Player player) {
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (!onlinePlayer.equals(player)) {
                onlinePlayer.showPlayer(this, player);
            }
        }
    }
}
