package not.hub.safetpa;

import not.hub.safetpa.util.DataPair;
import org.bukkit.entity.Player;

import java.util.UUID;

public record Request(DataPair target, DataPair requester) {
    boolean isSamePlayers(Player target, Player requester) {
        return this.target.uuid().equals(target.getUniqueId()) && this.requester.uuid().equals(requester.getUniqueId());
    }
}
