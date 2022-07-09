package not.hub.safetpa;

import not.hub.safetpa.util.Pair;
import org.bukkit.entity.Player;

import java.util.UUID;

public record Request(Pair<UUID, String> target, Pair<UUID, String> requester) {
    boolean isSamePlayers(Player target, Player requester) {
        return this.target.left().equals(target.getUniqueId()) && this.requester.left().equals(requester.getUniqueId());
    }
}
