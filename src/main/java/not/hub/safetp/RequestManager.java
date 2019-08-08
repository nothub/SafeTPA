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
                SafeTP.sendMessage(request.getRequester(), ChatColor.GOLD + "Your teleport request to " + ChatColor.RESET + request.getTarget().getDisplayName() + ChatColor.GOLD + " timed out.");
                pendingRequests.remove(request);
            }
        });

    }

    void addRequest(Player target, Player requester) {
        addRequest(new Request(target, requester));
    }

    private void addRequest(Request newRequest) {
        pendingRequests.forEach((request, date) -> {
            if (request.isSamePlayers(newRequest)) {
                pendingRequests.remove(request);
            }
        });
        pendingRequests.put(newRequest, new Date());
    }

    void removeRequest(Player target, Player requester) {
        removeRequest(new Request(target, requester));
    }

    private void removeRequest(Request toDeleteRequest) {
        pendingRequests.forEach((request, date) -> {
            if (request.isSamePlayers(toDeleteRequest)) {
                pendingRequests.remove(request);
            }
        });
    }

    private boolean containsRequest(Request searchedRequest) {
        AtomicBoolean exists = new AtomicBoolean(false);
        pendingRequests.forEach((request, date) -> {
            if (request.isSamePlayers(searchedRequest)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

    private boolean containsRequester(Player requester) {
        AtomicBoolean exists = new AtomicBoolean(false);
        pendingRequests.forEach((request, date) -> {
            if (request.getRequester().equals(requester)) {
                exists.set(true);
            }
        });
        return exists.get();
    }

    boolean isRequestExisting(Player target, Player requester) {
        return containsRequest(new Request(target, requester));
    }

    boolean isRequester(Player requester) {
        return containsRequester(requester);
    }

}
