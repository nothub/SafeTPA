package not.hub.safetp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class RequestManager {

    private final ConcurrentHashMap<Request, Date> pendingRequests;

    RequestManager() {
        this.pendingRequests = new ConcurrentHashMap<>();
    }

    void clearOldRequests(int timeoutValue) {

        Date now = new Date();

        pendingRequests.forEach((request, date) -> {
            if (((now.getTime() - date.getTime()) / 1000) > timeoutValue) {
                pendingRequests.remove(request);
                SafeTP.sendMessage(request.getRequester(), ChatColor.GOLD + "Your teleport request to " + ChatColor.RESET + request.getTarget().getDisplayName() + ChatColor.GOLD + " timed out.");
                SafeTP.sendMessage(request.getTarget(), ChatColor.GOLD + "The teleport request from " + ChatColor.RESET + request.getRequester().getDisplayName() + ChatColor.GOLD + " timed out.");
            }
        });

    }

    void addRequest(Player target, Player requester) {
        removeRequests(target, requester);
        pendingRequests.put(new Request(target, requester), new Date());
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
            if (request.getTarget().getUniqueId().equals(target.getUniqueId())) {
                pendingRequests.remove(request);
            }
        });
    }

    public void removeRequestsByRequester(Player requester) {
        pendingRequests.forEach((request, date) -> {
            if (request.getRequester().getUniqueId().equals(requester.getUniqueId())) {
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
        AtomicBoolean exists = new AtomicBoolean(false);
        pendingRequests.forEach((request, date) -> {
            if (request.getTarget().equals(target)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

    boolean isRequestActiveByRequester(Player requester) {
        AtomicBoolean exists = new AtomicBoolean(false);
        pendingRequests.forEach((request, date) -> {
            if (request.getRequester().equals(requester)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

}
