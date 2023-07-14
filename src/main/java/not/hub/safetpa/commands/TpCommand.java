package not.hub.safetpa.commands;

import not.hub.safetpa.Plugin;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class TpCommand {

    final Plugin plugin;
    public final String label;
    public final List<String> aliases;
    public final String description;
    public final String usage;

    public TpCommand(Plugin plugin, String label, List<String> aliases, String description, String usage) {
        this.plugin = plugin;
        this.label = label;
        this.aliases = aliases;
        this.description = description;
        this.usage = usage;
    }

    public TpCommand(Plugin plugin, PluginCommand pluginCommand) {
        this(plugin,
            pluginCommand.getLabel(),
            pluginCommand.getAliases().stream().toList(),
            pluginCommand.getDescription(),
            pluginCommand.getUsage());
    }

    public abstract boolean run(Player commandSender, String targetName);

    public void sendUsage(Player player) {
        player.sendMessage(usage);
    }

}
