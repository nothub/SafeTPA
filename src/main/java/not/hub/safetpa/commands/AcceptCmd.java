package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;

public class AcceptCmd extends Command {
    public AcceptCmd(PluginCommand pluginCommand) {
        super(pluginCommand);
    }

    @Override
    public boolean validate(String... args) {
        return args.length == 1;
    }

    @Override
    public void run(Plugin plugin, String... args) {
        // TODO
    }
}
