package not.hub.safetp;

import io.papermc.lib.PaperLib;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class SafeTP extends JavaPlugin {

    private RequestManager requestsManager = new RequestManager();

    private static String[] loadMessages = {"SafeTP works best if you run Paper Spigot on a Toaster!", "Join 0b0t.org, the Worlds oldest Minecraft Server!", "(^_^) (0w0) (^.^)"};
    private static int timeoutValue = 0;

    @Override
    public void onEnable() {
        loadConfig();
        timeoutValue = getConfig().getInt("request-timeout-seconds");
        PaperLib.suggestPaper(this);
        Random rand = new Random();
        getLogger().info(ChatColor.LIGHT_PURPLE + loadMessages[rand.nextInt(loadMessages.length)] + ChatColor.RESET);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String commandLabel, String[] args) {

        Player sender = null;

        if (commandSender instanceof Player) {
            sender = (Player) commandSender;
        }

        if (sender == null) {
            return false;
        }

        if (args.length == 0) {
            sendMessage(sender, ChatColor.GOLD + "You need to run this Command with an Argument, like this:");
            sendMessage(sender, "/tpa NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpy NAME " + ChatColor.GOLD + ".. or .. " + ChatColor.RESET + "/tpn NAME");
            return false;
        }

        if (isInvalidTarget(args[0])) {
            sendMessage(sender, ChatColor.GOLD + "Player not found.");
            return false;
        }

        if (sender.getName().equalsIgnoreCase(args[0])) {
            sendMessage(sender, ChatColor.GOLD + "You cant run this Command on yourself!");
            return false;
        }

        if (command.getLabel().equalsIgnoreCase("tpa")) {
            asktTP(getServer().getPlayer(args[0]), sender);
            return true;
        }

        if (command.getLabel().equalsIgnoreCase("tpy")) {
            acceptTP(sender, getServer().getPlayer(args[0]));
            return true;
        }

        if (command.getLabel().equalsIgnoreCase("tpn")) {
            denyTP(sender, getServer().getPlayer(args[0]));
            return true;
        }

        return false;

    }

    private void asktTP(Player tpTarget, Player tpRequester) {

        if (tpTarget == null || tpRequester == null) {
            return;
        }

        sendMessage(tpRequester, ChatColor.GOLD + "Request sent to: " + ChatColor.RESET + tpTarget.getDisplayName());
        sendMessage(tpTarget, tpRequester.getDisplayName() + ChatColor.GOLD + " wants to teleport to you.");
        sendMessage(tpTarget, ChatColor.GOLD + "Type " + ChatColor.RESET + "/tpy " + tpRequester.getName() + ChatColor.GOLD + " to accept or " + ChatColor.RESET + "/tpn " + tpRequester.getName() + ChatColor.GOLD + " to deny.");

        requestsManager.addRequest(tpTarget, tpRequester);

    }

    private void acceptTP(Player tpTarget, Player tpRequester) {

        if (tpTarget == null || tpRequester == null) {
            return;
        }

        if (requestsManager.requestNotExisting(tpTarget, tpRequester)) {
            sendMessage(tpTarget, ChatColor.GOLD + "There is no Request to accept from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.GOLD + "!");
            return;
        }

        sendMessage(tpTarget, ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.GREEN + " accepted" + ChatColor.GOLD + "!");
        sendMessage(tpRequester, ChatColor.GOLD + "Your Request was " + ChatColor.GREEN + "accepted" + ChatColor.GOLD + ", teleporting to: " + ChatColor.RESET + tpTarget.getDisplayName());

        executeTP(tpTarget, tpRequester);
        requestsManager.removeRequest(tpTarget, tpRequester);

    }

    private void denyTP(Player tpTarget, Player tpRequester) {

        if (tpTarget == null || tpRequester == null) {
            return;
        }

        if (requestsManager.requestNotExisting(tpTarget, tpRequester)) {
            sendMessage(tpTarget, ChatColor.GOLD + "There is no Request to deny from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.GOLD + "!");
            return;
        }

        sendMessage(tpTarget, ChatColor.GOLD + "Request from " + ChatColor.RESET + tpRequester.getDisplayName() + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        sendMessage(tpRequester, ChatColor.GOLD + "Your Request sent to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.GOLD + " was" + ChatColor.RED + " denied" + ChatColor.GOLD + "!");
        requestsManager.removeRequest(tpTarget, tpRequester);

    }

    private void executeTP(Player tpTarget, Player tpRequester) {

        if (tpTarget == null || tpRequester == null) {
            return;
        }

        getLogger().info("Teleporting " + tpRequester.getName() + " to " + tpTarget.getName());

        // vanishing player to defeat evil tp exploit demons
        for (Player player : getServer().getOnlinePlayers()) {
            if (!player.equals(tpRequester)) {
                player.hidePlayer(this, tpRequester);
            }
        }

        PaperLib.teleportAsync(tpRequester, tpTarget.getLocation()).thenAccept(result -> {

            if (result) {
                sendMessage(tpTarget, tpRequester.getDisplayName() + ChatColor.GOLD + " teleported to you!");
                sendMessage(tpRequester, ChatColor.GOLD + "Teleported to " + ChatColor.RESET + tpTarget.getDisplayName() + ChatColor.GOLD + "!");
            } else {
                sendMessage(tpTarget, ChatColor.RED + "Teleport failed, you should harass your Admin because of this!");
                sendMessage(tpRequester, ChatColor.RED + "Teleport failed, you should harass your Admin because of this!");
            }

        });

        // unvanish player
        for (Player player : getServer().getOnlinePlayers()) {
            if (!player.equals(tpRequester)) {
                player.showPlayer(this, tpRequester);
            }
        }

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

    private String sanitizeUsername(String name) {

        name = name.replaceAll("[^a-zA-Z0-9_]", "");

        if (name.length() < 3 || name.length() > 16) {
            return null;
        }

        return name;

    }

    static void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }

    private void loadConfig() {
        getConfig().addDefault("request-timeout-seconds", 60);
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    static int getTimeoutValue() {
        return timeoutValue;
    }

}
