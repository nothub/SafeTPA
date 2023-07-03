package not.hub.safetpa;

import io.papermc.lib.PaperLib;
import lombok.Getter;
import not.hub.safetpa.listeners.MoveListener;
import not.hub.safetpa.util.Ignores;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import java.util.regex.Pattern;

public final class Plugin extends JavaPlugin {

    private static final String BLOCKED_PREFIX = "requests-blocked-";
    private static final Pattern USERNAME_SANITISATION_PATTERN = Pattern.compile("[^a-zA-Z\\d_ ]");

    @Getter
    private final RequestManager requestManager = new RequestManager();

    public static void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }

    private static String sanitizeUsername(String name) {
        name = USERNAME_SANITISATION_PATTERN.matcher(name).replaceAll("");
        if (name.length() < 1 || name.length() > 16) {
            return null;
        }
        return name;
    }

    @Override
    public void onEnable() {
        PaperLib.suggestPaper(this);

        new Metrics(this, 11798);

        loadConfig();

        Ignores.dir = Path.of(getConfig().getString("ignores-path"));

        getServer().getPluginManager().registerEvents(new MoveListener(this), this);

        getServer().getScheduler().runTaskTimer(this, this::clearOldRequests, 20, 20);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, String[] args) {
        if (!(commandSender instanceof Player sender))
            return true;

        // 0 arg commands

        if (command.getLabel().equalsIgnoreCase("tpt")) {
            toggleRequestBlock(sender);
            return true;
        }

        // > 0 arg commands

        if (args.length == 0) {
            sendMessage(sender, ChatColor.GOLD + "You need to run this command with an argument, like this:");
            sendMessage(sender, "/tpa NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpy NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpn NAME" + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/ignoretp NAME");
            return false;
        }

        // If this check is passed (target is valid / if case is not executed),
        // args[0] is guranteed to contain the name of a player that currently is online.
        if (isInvalidTarget(args[0])) {
            sendMessage(sender, ChatColor.GOLD + "Player not found.");
            return false;
        }
        Player target = getServer().getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, ChatColor.GOLD + "Player not found.");
            return false;
        }

        if (sender.getName().equalsIgnoreCase(args[0])) {
            // Target is sender, we just do nothing.
            sendMessage(sender, ChatColor.GOLD + "Teleported to " + ChatColor.RESET + sender.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
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
            sendMessage(sender, "No longer ignoring tp requests from " + target.getName());
        } else {
            boolean success = Ignores.set(sender.getUniqueId(), target.getUniqueId(), true);
            if (success) {
                sendMessage(sender, "Ignoring tp requests from " + target.getName());
            } else {
                sendMessage(sender, ChatColor.RED + "Maximum reached, can not add more ignores!");
            }
        }
    }

    public void clearOldRequests() {
        requestManager.clearOldRequests(getConfig().getInt("request-timeout-seconds"));
    }

    private void askTP(Player tpTarget, Player tpRequester) {
        if (Ignores.get(tpTarget.getUniqueId(), tpRequester.getUniqueId())) {
            sendMessage(tpRequester, tpTarget.getName() + " is ignoring your tpa requests!");
        }

        if (getConfig().getBoolean("spawn-tp-deny") && isAtSpawn(tpRequester)) {
            getLogger().info("Denying teleport request while in spawn area from " + tpRequester.getName() + " to " + tpTarget.getName());
            sendMessage(tpRequester, ChatColor.GOLD + "You are not allowed to teleport while in the spawn area!");
            return;
        }

        if (isRequestBlock(tpTarget)) {
            sendMessage(tpRequester, tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " is currently not accepting any teleport requests!");
            return;
        }

        if (getConfig().getBoolean("distance-limit") &&
            getOverworldXzVector(tpRequester).distance(getOverworldXzVector(tpTarget)) > getConfig().getInt("distance-limit-radius")) {
            getLogger().info("Denying teleport request while out of range from " + tpRequester.getName() + " to " + tpTarget.getName());
            sendMessage(tpRequester, ChatColor.GOLD + "You are too far away from " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to teleport!");
            return;
        }

        if (requestManager.isRequestActive(tpTarget, tpRequester)) {
            sendMessage(tpRequester, ChatColor.GOLD + "Please wait for " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to accept or deny your request.");
            return;
        }

        if (!getConfig().getBoolean("allow-multi-target-request") && requestManager.isRequestActiveByRequester(tpRequester)) {
            sendMessage(tpRequester, ChatColor.GOLD + "Please wait for your existing request to be accepted or denied.");
            return;
        }

        sendMessage(tpRequester, ChatColor.GOLD + "Request sent to: " + ChatColor.RESET + tpTarget.getDisplayName());
        sendMessage(tpTarget, tpRequester.getDisplayName() + "" + ChatColor.GOLD + " wants to teleport to you.");
        sendMessage(tpTarget, ChatColor.GOLD + "Type " + ChatColor.RESET + "/tpy " + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " to accept or " + ChatColor.RESET + "/tpn " + tpRequester.getDisplayName() + ChatColor.GOLD + " to deny.");

        requestManager.addRequest(tpTarget, tpRequester);

    }

    private void acceptTP(Player tpTarget, Player tpRequester) {
        if (!requestManager.isRequestActive(tpTarget, tpRequester)) {
            sendMessage(tpTarget, ChatColor.GOLD + "There is no request to accept from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            return;
        }

        sendMessage(tpTarget, ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GREEN + " accepted" + ChatColor.GOLD + "!");
        sendMessage(tpRequester, ChatColor.GOLD + "Your request was " + ChatColor.GREEN + "accepted" + ChatColor.GOLD + ", teleporting to: " + ChatColor.RESET + tpTarget.getDisplayName());

        executeTP(tpTarget, tpRequester);
        requestManager.removeRequests(tpTarget, tpRequester);

    }

    private void denyTP(Player tpTarget, Player tpRequester) {
        if (!requestManager.isRequestActive(tpTarget, tpRequester)) {
            sendMessage(tpTarget, ChatColor.GOLD + "There is no request to deny from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            return;
        }

        sendMessage(tpTarget, ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        sendMessage(tpRequester, ChatColor.GOLD + "Your request sent to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " was" + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        requestManager.removeRequests(tpTarget, tpRequester);
    }

    private void executeTP(Player tpTarget, Player tpRequester) {
        if (tpTarget == null || tpRequester == null) {
            return;
        }

        // deny mounted target or requester
        if (tpTarget.getVehicle() != null || tpRequester.getVehicle() != null) {
            sendMessage(tpTarget, ChatColor.RED + "Teleport failed!");
            sendMessage(tpRequester, ChatColor.RED + "Teleport failed!");
            return;
        }

        int tpDelay = getConfig().getInt("tp-delay-seconds");
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
                sendMessage(tpTarget, tpRequester.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + " teleported to you!");
                sendMessage(tpRequester, ChatColor.GOLD + "Teleported to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.RESET + ChatColor.GOLD + "!");
            } else {
                sendMessage(tpTarget, ChatColor.RED + "Teleport failed, you should harass your admin because of this!");
                sendMessage(tpRequester, ChatColor.RED + "Teleport failed, you should harass your admin because of this!");
            }
        }).thenAccept(ignored -> {
            // Unvanish requester after n ticks
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> this.unvanish(tpRequester), getConfig().getInt("unvanish-delay-ticks"));
        });
    }

    private void toggleRequestBlock(Player toggleRequester) {
        if (toggleRequester == null) {
            return;
        }

        if (isRequestBlock(toggleRequester)) {
            getConfig().set(BLOCKED_PREFIX + toggleRequester.getUniqueId(), null); // if toggle is getting turned off, we delete instead of setting false
            sendMessage(toggleRequester, ChatColor.GOLD + "Request are now " + ChatColor.GREEN + " enabled" + ChatColor.GOLD + "!");
        } else {
            getConfig().set(BLOCKED_PREFIX + toggleRequester.getUniqueId(), true);
            requestManager.removeRequestsByTarget(toggleRequester);
            sendMessage(toggleRequester, ChatColor.GOLD + "Request are now " + ChatColor.RED + " disabled" + ChatColor.GOLD + "!");
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
        return pos.getX() <= getConfig().getInt("spawn-tp-deny-radius") && pos.getZ() <= getConfig().getInt("spawn-tp-deny-radius");
    }

    private boolean isInvalidTarget(String args) {
        // check for empty argument
        if (args.isEmpty()) {
            return true;
        }
        // check for invalid usernames
        String target = sanitizeUsername(args);
        if (target == null) {
            return true;
        }
        // check if player is online
        return getServer().getPlayer(target) == null;
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

    private void loadConfig() {
        // defaults
        getConfig().addDefault("allow-multi-target-request", true);
        getConfig().addDefault("request-timeout-seconds", 60);
        getConfig().addDefault("unvanish-delay-ticks", 1);
        getConfig().addDefault("spawn-tp-deny", true);
        getConfig().addDefault("spawn-tp-deny-radius", 1500);
        getConfig().addDefault("distance-limit", false);
        getConfig().addDefault("distance-limit-radius", 10000);
        getConfig().addDefault("tp-delay-seconds", 0);
        getConfig().addDefault("ignores-path", "");
        getConfig().options().copyDefaults(true);
        saveConfig();

        // validate

        if (getConfig().getInt("request-timeout-seconds") < 10) {
            getConfig().set("request-timeout-seconds", 10);
            saveConfig();
        }

        if (getConfig().getInt("unvanish-delay-ticks") < 1) {
            getConfig().set("unvanish-delay-ticks", 1);
            saveConfig();
        }

        if (getConfig().getInt("spawn-tp-deny-radius") < 16) {
            getConfig().set("spawn-tp-deny-radius", 16);
            saveConfig();
        }

        if (getConfig().getInt("distance-limit-radius") < 16) {
            getConfig().set("distance-limit-radius", 16);
            saveConfig();
        }

        if (getConfig().getInt("tp-delay-seconds") < 0) {
            getConfig().set("tp-delay-seconds", 0);
            saveConfig();
        }

        if (getConfig().getString("ignores-path") == null || getConfig().getString("ignores-path").isBlank()) {
            getConfig().set("ignores-path", getDataFolder().toPath().resolve("ignores").toString());
            saveConfig();
        }
        try {
            Path.of(getConfig().getString("ignores-path"));
        } catch (InvalidPathException ex) {
            getLogger().warning("Invalid ignores-path config: " + ex.getMessage());
            getConfig().set("ignores-path", getDataFolder().toPath().resolve("ignores").toString());
            saveConfig();
        }
        getLogger().info("Storing ignore lookup files to: " + Path.of(getConfig().getString("ignores-path")).toAbsolutePath());

    }
}
