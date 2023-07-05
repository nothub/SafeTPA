package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class Command {
    public final String label;
    public final List<String> aliases;
    public final String description;
    public final String usage;
    private final int argsMin;
    private final int argsMax;

    public Command(String label, List<String> aliases, String description, String usage, int argsMin, int argsMax) {
        this.label = label;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
        this.argsMin = argsMin;
        this.argsMax = argsMax;
    }

    public Command(PluginCommand pluginCommand, int argsMin, int argsMax) {
        this(pluginCommand.getLabel(),
            pluginCommand.getAliases().stream().toList(),
            pluginCommand.getDescription(),
            pluginCommand.getUsage(),
            argsMin, argsMax);
    }

    boolean validate(String... args) {
        return args.length >= argsMin && args.length <= argsMax;
    }

    public boolean invoke(Plugin plugin, Player sender, String... args) {
        if (!validate(args)) {
            sender.sendMessage(usage);
            return false;
        }
        return run(plugin, sender, args);
    }

    abstract boolean run(Plugin plugin, Player sender, String... args);
}
