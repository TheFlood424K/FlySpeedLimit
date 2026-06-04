package net.medievalnetwork.flyspeedlimit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class AdminCommand implements CommandExecutor {

    private final FlySpeedLimit plugin;

    public AdminCommand(FlySpeedLimit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("flyspeedlimit.admin")) {
            sender.sendMessage(plugin.cfg().msg("no-permission"));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.cfg().reload();
            sender.sendMessage(plugin.cfg().msg("reload-success"));
            return true;
        }
        sender.sendMessage("\u00a7eUsage: /" + label + " reload");
        return true;
    }
}
