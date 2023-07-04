package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;

public class IgnoreCmd extends Command {
    public IgnoreCmd(PluginCommand pluginCommand) {
        super(pluginCommand);
    }

    @Override
    public boolean validate(String... args) {
        return args.length == 0 || args.length == 1;
    }

    @Override
    public void run(Plugin plugin, String... args) {
        // TODO
    }
}
