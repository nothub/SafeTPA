package not.hub.safetp;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

class RequestManager {

    private final ConcurrentHashMap<Request, Date> pendingRequests;

    RequestManager() {
        this.pendingRequests = new ConcurrentHashMap<>();
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                clearOldRequests();
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private void clearOldRequests() {
        Date now = new Date();
        pendingRequests.forEach((request, date) -> {
            if (((now.getTime() - date.getTime()) / 1000) > SafeTP.getTimeoutValue()) {
                SafeTP.sendMessage(request.getRequester(), ChatColor.GOLD + "Your Teleport Request for " + ChatColor.RESET + request.getTarget().getDisplayName() + ChatColor.RED + " timed out.");
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

    boolean requestNotExisting(Player target, Player requester) {
        return !containsRequest(new Request(target, requester));
    }

}
