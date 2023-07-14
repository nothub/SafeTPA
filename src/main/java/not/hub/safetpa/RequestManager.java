package not.hub.safetpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RequestManager {
    private static final Map<Request, Long> pendingRequests = new ConcurrentHashMap<>();

    static void clearOldRequests(int timeoutValue) {
        long time = System.currentTimeMillis();
        pendingRequests.forEach((request, requestTime) -> {
            if (((time - requestTime) / 1000L) > timeoutValue) {
                pendingRequests.remove(request);
                Player requester = Bukkit.getPlayer(request.requester().uuid());
                if (requester != null) {
                    requester.sendMessage(ChatColor.GOLD + "Your teleport request to " + ChatColor.RESET + request.target().name() + ChatColor.GOLD + " timed out.");
                }
                Player target = Bukkit.getPlayer(request.target().uuid());
                if (target != null) {
                    target.sendMessage(ChatColor.GOLD + "The teleport request from " + ChatColor.RESET + request.requester().name() + ChatColor.GOLD + " timed out.");
                }
            }
        });
    }

    public static void addRequest(Player target, Player requester) {
        removeRequests(target, requester);
        pendingRequests.put(Request.of(target, requester), System.currentTimeMillis());
    }

    public static void removeRequests(Player target, Player requester) {
        for (Request request : pendingRequests.keySet()) {
            if (request.isSamePlayers(target, requester)) {
                pendingRequests.remove(request);
            }
        }
    }

    public static void cancelRequestsByTarget(Player target) {
        for (Request request : pendingRequests.keySet()) {
            if (request.target().uuid().equals(target.getUniqueId())) {
                pendingRequests.remove(request);
                // TODO: cancel info message
            }
        }
    }

    public static void cancelRequestsByRequester(Player requester) {
        for (Request request : pendingRequests.keySet()) {
            if (request.requester().uuid().equals(requester.getUniqueId())) {
                pendingRequests.remove(request);
                // TODO: cancel info message
            }
        }
    }

    public static boolean isRequestActive(Player target, Player requester) {
        for (Request request : pendingRequests.keySet()) {
            if (request.isSamePlayers(target, requester)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRequestActiveByTarget(Player target) {
        for (Request request : pendingRequests.keySet()) {
            if (request.target().uuid().equals(target.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRequestActiveByRequester(Player requester) {
        for (Request request : pendingRequests.keySet()) {
            if (request.requester().uuid().equals(requester.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

}
