package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class Command {
    public final String label;

    public final Set<String> aliases;

    public final String description;

    public final String usage;

    public Command(String label, Set<String> aliases, String description, String usage) {
        this.label = label;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
    }

    public Command(PluginCommand pluginCommand) {
        this(pluginCommand.getLabel(),
            pluginCommand.getAliases().stream().collect(Collectors.toUnmodifiableSet()),
            pluginCommand.getDescription(),
            pluginCommand.getUsage());
    }

    public abstract boolean validate(String... args);

    public abstract void run(Plugin plugin, String... args);
}
