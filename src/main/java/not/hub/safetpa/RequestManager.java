package not.hub.safetpa;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RequestManager {
    private final Map<Request, Long> pendingRequests = new ConcurrentHashMap<>();

    void clearOldRequests(int timeoutValue) {
        long time = System.currentTimeMillis();
        pendingRequests.forEach((request, requestTime) -> {
            if (((time - requestTime) / 1000) > timeoutValue) {
                pendingRequests.remove(request);
                Player requester = Bukkit.getPlayer(request.requester().uuid());
                if (requester != null) {
                    Plugin.sendMessage(requester, ChatColor.GOLD + "Your teleport request to " + ChatColor.RESET + request.target().name() + ChatColor.GOLD + " timed out.");
                }
                Player target = Bukkit.getPlayer(request.target().uuid());
                if (target != null) {
                    Plugin.sendMessage(target, ChatColor.GOLD + "The teleport request from " + ChatColor.RESET + request.requester().name() + ChatColor.GOLD + " timed out.");
                }
            }
        });
    }

    void addRequest(Player target, Player requester) {
        removeRequests(target, requester);
        pendingRequests.put(Request.of(target,requester), System.currentTimeMillis());
    }

    void removeRequests(Player target, Player requester) {
        pendingRequests.forEach((request, date) -> {
            if (request.isSamePlayers(target, requester)) {
                pendingRequests.remove(request);
            }
        });
    }

    public void removeRequestsByTarget(Player target) {
        pendingRequests.forEach((request, date) -> {
            if (request.target().uuid().equals(target.getUniqueId())) {
                pendingRequests.remove(request);
            }
        });
    }

    public void removeRequestsByRequester(Player requester) {
        pendingRequests.forEach((request, date) -> {
            if (request.requester().uuid().equals(requester.getUniqueId())) {
                pendingRequests.remove(request);
            }
        });
    }

    boolean isRequestActive(Player target, Player requester) {
        AtomicBoolean exists = new AtomicBoolean(false);
        pendingRequests.forEach((request, date) -> {
            if (request.isSamePlayers(target, requester)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

    boolean isRequestActiveByTarget(Player target) {
        for (Map.Entry<Request, Long> entry : pendingRequests.entrySet()) {
            Request request = entry.getKey();
            if (request.target().uuid().equals(target.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    boolean isRequestActiveByRequester(Player requester) {
        for (Map.Entry<Request, Long> entry : pendingRequests.entrySet()) {
            Request request = entry.getKey();
            if (request.requester().uuid().equals(requester.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public Optional<Request> getRequestByRequester(Player requester) {
        for (Map.Entry<Request, Long> entry : pendingRequests.entrySet()) {
            Request request = entry.getKey();
            if (request.requester().uuid().equals(requester.getUniqueId())) {
                return Optional.of(request);
            }
        }
        return Optional.empty();
    }
}
