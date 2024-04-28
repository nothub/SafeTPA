package lol.hub.safetpa;

import org.bukkit.entity.Player;

public record Request(PlayerData target, PlayerData requester) {

    public static Request of(PlayerData target, PlayerData requester) {
        return new Request(target, requester);
    }

    static Request of(Player target, Player requester) {
        return new Request(PlayerData.of(target), PlayerData.of(requester));
    }

    boolean isSamePlayers(Player target, Player requester) {
        return this.target.uuid().equals(target.getUniqueId()) && this.requester.uuid().equals(requester.getUniqueId());
    }

}
