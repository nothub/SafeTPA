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

    boolean isSamePlayers(Player target, Player requester) {
        return this.target.equals(target) && this.requester.equals(requester);
    }

}
