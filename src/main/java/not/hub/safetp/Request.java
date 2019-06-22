package not.hub.safetp;

import org.bukkit.entity.Player;

class Request {

    private final Player target;
    private final Player requester;

    Request(Player target, Player requester) {
        this.target = target;
        this.requester = requester;
    }

    Player getTarget() {
        return target;
    }

    Player getRequester() {
        return requester;
    }

    boolean isSamePlayers(Request newRequest) {
        return this.target.equals(newRequest.target) && this.requester.equals(newRequest.requester);
    }

}
