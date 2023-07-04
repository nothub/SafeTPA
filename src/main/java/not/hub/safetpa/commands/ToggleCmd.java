package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;

public class ToggleCmd extends Command {
    public ToggleCmd(PluginCommand pluginCommand) {
        super(pluginCommand);
    }

    @Override
    public boolean validate(String... args) {
        return args.length == 0;
    }

    @Override
    public void run(Plugin plugin, String... args) {
        // TODO
    }
}
