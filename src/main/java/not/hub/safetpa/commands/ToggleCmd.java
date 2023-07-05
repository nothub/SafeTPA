package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class ToggleCmd extends Command {
    public ToggleCmd(PluginCommand pluginCommand) {
        super(pluginCommand, 0, 0);
    }

    @Override
    public boolean run(Plugin plugin, Player sender, String... args) {
        // TODO
        return false;
    }
}
