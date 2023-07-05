package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class IgnoreCmd extends Command {
    public IgnoreCmd(PluginCommand pluginCommand) {
        super(pluginCommand, 0, 1);
    }

    @Override
    public boolean run(Plugin plugin, Player sender, String... args) {
        // TODO
        return false;
    }
}
