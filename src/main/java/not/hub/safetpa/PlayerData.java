package not.hub.safetpa;

import org.bukkit.entity.Player;

import java.util.UUID;

public record PlayerData(String name, UUID uuid) {

    public static PlayerData of(Player player) {
        return new PlayerData(player.getName(), player.getUniqueId());
    }

}
