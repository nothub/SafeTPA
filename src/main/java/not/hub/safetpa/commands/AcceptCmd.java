package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class AcceptCmd extends Command {
    public AcceptCmd(PluginCommand pluginCommand) {
        super(pluginCommand, 1, 1);
    }

    @Override
    public boolean run(Plugin plugin, Player sender, String... args) {
        // TODO
        return false;
    }
}
